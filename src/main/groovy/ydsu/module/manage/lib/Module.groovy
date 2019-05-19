package ydsu.module.manage.lib

import com.google.common.io.Files
import groovy.io.FileType
import groovy.util.logging.Slf4j
import ydsu.module.util.lib.Exec

import java.nio.file.Paths
import java.util.regex.Matcher
import java.util.regex.Pattern

@Slf4j
class Module {
    private final static File USER_HOME = new File(System.getProperty('user.home'))
    private final static File YDSU_HOME = new File(USER_HOME, 'appdata/ydsu/')
    private final static File YDSU_CACHE = new File(YDSU_HOME, 'cache/')
    private final static String PACKAGE_REGEX = /^package (.+)/
    private final static Pattern PACKAGE_PATTERN = Pattern.compile(PACKAGE_REGEX)
    private TreeSet<String> systemPath
    private TreeSet<String> classPath

    Module(TreeSet<String> systemPath, TreeSet<String> classPath) {
        this.systemPath = systemPath
        this.classPath = classPath
    }

    static Module newInstance(boolean autoPull, String localPathOrGitUrl) {
        TreeSet<String> systemPath
        if (localPathOrGitUrl.startsWith('https://github.com')) {
            systemPath = pullRepoAndGetLocalPath(autoPull, localPathOrGitUrl)
        } else {
            systemPath = getLocalPath(localPathOrGitUrl)
        }
        TreeSet<String> classPath = getClassPath(systemPath)
        return new Module(systemPath, classPath)
    }

    private static TreeSet<String> pullRepoAndGetLocalPath(boolean autoPull, String gitUrl) {
        YDSU_CACHE.mkdirs()
        /**
         * Example: https://github.com/yesday/ydsu-module/master/src/main/groovy/ydsu/module/skeleton
         * Parse the git url and the sub path /path/to/module/or/modules
         * Check if YDSU_CACHE/repo exists
         * If yes and autoPull is true then pull
         * If repo does not exist in YDSU_CACHE then clone it to YDSU_CACHE within the sub path
         * of the ownerowner (yesday/ydsu will be the repo).
         * Locate the sub path of module or modules within the YDSU_CACHE.
         * Call getLocalPath(sub path of module or modules within the YDSU_CACHE)
         */
        ModuleGitUrl url
        try {
            url = ModuleGitUrl.newInstance(gitUrl)
        } catch (Exception e) {
            log.warn "Could not parse module URL $gitUrl: ${e.getMessage()}"
            return []
        }
        File localModulePath = Paths.get(YDSU_CACHE.toString(), url.owner, url.repository, url.modulePath).toFile()
        File localRepoOwnerPath = new File(YDSU_CACHE, url.owner)
        if (localModulePath.exists()) {
            if (autoPull) {
                Exec.command('git pull', localModulePath)
            }
        } else {
            localRepoOwnerPath.mkdirs()
            String cmd = "GIT_TERMINAL_PROMPT=0 git clone -b ${url.branch} ${url.httpsCloneUrl}"
            try {
                Exec.command(cmd, localRepoOwnerPath)
            } catch (RuntimeException e) {
                log.warn "Could not load module URL {}: {}", gitUrl, e.message
                return []
            }
        }
        return getLocalPath(localModulePath.toString())
    }

    private static TreeSet<String> getLocalPath(String localPath) {
        TreeSet<String> systemPath = new TreeSet<>()
        String path
        if (localPath.startsWith('~')) {
            path = localPath.replace('~', USER_HOME.toString())
        } else {
            path = localPath
        }
        if (path.startsWith('/')) {
            File dir = new File(path)
            if (dir.exists() && dir.name != '.git') {
                /**
                 * If the localPath directory has at least one file then it is a single module.
                 * Otherwise, it's a collection of modules.
                 */
                if (dir.listFiles({ it.isFile() } as FileFilter)) {
                    systemPath << dir.toString()
                    log.info "Loaded single module {}", dir.name
                } else {
                    dir.eachDir {
                        systemPath << it.toString()
                    }
                    log.info "Loaded collection of modules {}", dir.name
                }
            }
        }
        systemPath
    }

    private static TreeSet<String> getClassPath(TreeSet<String> systemPath) {
        TreeSet<String> classPath = new TreeSet<>()
        systemPath.each {
            // Each element in system path is always a single flattened module, never a collection of modules.
            File moduleDir = new File(it)
            // We need to find the parent directory of the root package. That is where the classpath starts.
            // For example, parentOfRootPackage for this class will be the src/main/groovy directory.
            File parentOfRootPackage = findParentOfRootPackage(moduleDir)
            // If parent of root package was found and is parent of module dir
            if (parentOfRootPackage && moduleDir.toString().startsWith(parentOfRootPackage.toString())) {
                classPath << parentOfRootPackage.toString()
            } else {
                classPath << moduleDir.toString()
            }
        }
        return classPath
    }

    private static File findParentOfRootPackage(File moduleDir) {
        List<File> srcFiles = new ArrayList<>()
        moduleDir.eachFileRecurse(FileType.FILES) {
            if (['java', 'groovy', 'kt'].contains(Files.getFileExtension(it.name))) {
                srcFiles << it
            }
        }
        for (File it : srcFiles) {
            Matcher m = PACKAGE_PATTERN.matcher(it.text)
            if (m.find()) {
                String pkg = it.text.substring(m.start() + 8, m.end())
                int count = pkg.count('.') + 1
                File parentOfRootPackage = it.parentFile
                count.times {
                    parentOfRootPackage = parentOfRootPackage.parentFile
                }
                return parentOfRootPackage
            }
        }
        return null
    }
}
