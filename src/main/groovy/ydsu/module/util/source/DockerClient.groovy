package ydsu.module.util.source

import com.google.common.base.Preconditions
import com.google.common.base.Strings
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import groovyjarjarantlr4.v4.runtime.misc.NotNull

import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

import static ydsu.module.util.source.Exec.command
import static ydsu.module.util.source.Exec.longCommand

/**
 * A minimalistic Docker client using the {@link Exec} utility and the {@code docker} command under the hood.
 *
 * Not thread-safe. Instances of this class are mutable. To use them concurrently, clients must surround each method
 * invocation (or invocation sequence) with external synchronization of the clients' choosing.
 */
@Slf4j
class DockerClient {
    //region Data members
    @NotBlank
    private String imageName
    private String imageDir
    @NotNull
    private Map<String, Tuple2<String, Boolean>> bindMounts = new LinkedHashMap<>()
    @NotNull
    private Map<String, String> volumes = new LinkedHashMap<>()
    @NotNull
    private Map<String, String> env = new LinkedHashMap<>()
    @NotNull
    private List<Tuple2<String, String>> publishedPorts = new ArrayList<>()
    private String containerId
    //endregion

    //region Constructors
    DockerClient(@NotBlank String imageName) {
        Args.notBlank('imageName', imageName)
        this.imageName = imageName
    }

    DockerClient(@NotBlank String imageName, @NotBlank String imageDir) {
        Args.notBlank('imageName', imageName)
        Args.notBlank('imageDir', imageDir)
        this.imageName = imageName
        this.imageDir = imageDir
    }
    //endregion

    //region Builder
    @NotNull
    @Valid
    DockerClient withBindMount(@NotBlank String hostPath, @NotBlank String containerPath, boolean readonly = true) {
        Args.notBlank('hostPath', hostPath)
        Args.notBlank('containerPath', containerPath)
        bindMounts.put(containerPath, new Tuple2<>(hostPath, readonly))
        this
    }

    @NotNull
    @Valid
    DockerClient withVolume(@NotBlank String hostPath, @NotBlank String containerPath) {
        Args.notBlank('hostPath', hostPath)
        Args.notBlank('containerPath', containerPath)
        volumes.put(containerPath, hostPath)
        this
    }

    @NotNull
    @Valid
    DockerClient withEnv(@NotBlank String key, String value) {
        Args.notBlank('key', key)
        env.put(key, value)
        this
    }

    @NotNull
    @Valid
    DockerClient withPublishedPorts(@NotEmpty Tuple2<String, String>... port) {
        Preconditions.checkArgument(port != null && port.size() > 0, "Argument 'port' must not be empty")
        for (int i = 0; i < port.length; i++) {
            Args.notBlank("port$i", port[i].v1)
        }
        publishedPorts.addAll(port)
        this
    }
    //endregion

    //region Public interface
    void run() {
        if (isRunning()) {
            throw new RuntimeException("container $containerId is already running")
        }

        if (!imageExists()) {
            if (imageDir) {
                log.info('docker image {} not found, building a new image from dir {}', imageName, imageDir)
                buildImage()
            } else {
                throw new RuntimeException("docker image $imageName not found")
            }
        }

        containerId = command(getRunCommand())
    }

    @NotNull
    String execInContainer(@NotBlank String cmd) {
        Args.notBlank('cmd', cmd)
        if (isRunning()) {
            // Example: docker exec -t b53d2d7d647b date
            return command("docker exec -t $containerId $cmd")
        } else {
            throw new RuntimeException('container is not running')
        }
    }

    @NotNull
    String execScriptInContainer(@NotBlank String multilineScript) {
        Args.notBlank('multilineScript', multilineScript)
        if (isRunning()) {
            // Example: String command = "docker exec -t b53d2d7d647b bash -c 'mkdir -p \$HOME/dir; pwd'"
            String cmd = multilineScript.lines().filter { !it.isBlank() }
            /**
             * Single quotes ('') are already used to preserve the literal value of the script passed to the container.
             * In bash a single quote may not occur between single quotes, even when preceded by a backslash. Therefore,
             * we replace the single quotes with double quotes.
             */
                    .collect { it.replaceAll("'", '"') }
                    .join('; ')
            return command("docker exec -t $containerId bash -c '$cmd'")
        } else {
            throw new RuntimeException('container is not running')
        }
    }

    void stopAndRemove() {
        if (isRunning()) {
            log.info command("docker stop $containerId")
            containerId = null
        } else {
            log.warn 'stopAndRemove: container is not running'
        }
    }

    boolean isRunning() {
        containerId
    }

    boolean imageExists() {
        !command("docker image ls $imageName").endsWith('SIZE')
    }

    void buildImage() {
        if (imageDir) {
            // Example: docker build --pull -t archlinux src/main/docker/archlinux
            longCommand("docker build --pull -t $imageName $imageDir")
        } else {
            throw new RuntimeException('cannot build an image: imageDir is null: create a new docker client ' +
                    'instance with imageDir and try again')
        }
    }

    void removeImage() {
        if (imageExists()) {
            log.info command("docker rmi $imageName")
        } else {
            log.info "image $imageName does not exist"
        }
    }
    //endregion

    //region Utility methods with package-private access enabled for unit tests
    @PackageScope
    @NotBlank
    String getRunCommand() {
        ['docker run --rm -d -it',
         getBindMountArgs(),
         getVolumeArgs(),
         getEnvArgs(),
         getPublishedPortArgs(),
         imageName].join(' \\\n')
    }

    @PackageScope
    @NotNull
    String getBindMountArgs() {
        // Example: --mount type=bind,source="$(pwd)"/target,target=/app,readonly
        bindMounts.collect { k, Tuple2<String, Boolean> v ->
            /--mount type=bind,source=${v.v1},target=${k}${
                v.v2 ? ',readonly' : ''
            }/
        }.join(' ')
    }

    @PackageScope
    @NotNull
    String getVolumeArgs() {
        // Example: -v $(pwd):/source
        volumes.collect { k, v ->
            /-v $v:$k/
        }.join(' ')
    }

    @PackageScope
    @NotNull
    String getEnvArgs() {
        // Example: -e "deep=purple" -e today
        env.collect { k, v ->
            "-e ${!Strings.isNullOrEmpty(v) && !v.trim().isEmpty() ? "\"$k=$v\"" : "$k"}"
        }.join(' ')
    }

    @PackageScope
    @NotNull
    String getPublishedPortArgs() {
        // Example: -p 127.0.0.1:80:8080/tcp
        publishedPorts.collect { Tuple2<String, String> p ->
            "-p ${!Strings.isNullOrEmpty(p.v2) && !p.v2.trim().isEmpty() ? "$p.v1:$p.v2" : "$p.v1"}"
        }.join(' ')
    }
    //endregion
}