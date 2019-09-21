package ydsu.module.util.source

import com.google.common.collect.ImmutableSet
import com.google.common.io.Files
import groovy.io.FileType
import groovy.transform.EqualsAndHashCode
import groovy.util.logging.Slf4j

import java.util.regex.Matcher
import java.util.regex.Pattern

@Slf4j
@EqualsAndHashCode
class ModuleMetadata {
    private static final String PACKAGE_REGEX = /^package (.+)/
    private static final Pattern PACKAGE_PATTERN = Pattern.compile(PACKAGE_REGEX)
    private String systemPathRoot
    private ImmutableSet<String> moduleNames
    /**
     * The root directory where the classpath starts. This has to be the parent directory that holds the upper package.
     * For example, for a class like {@link ydsu.module.util.source.Module} this directory will be the
     * {@code src/main/groovy} because it is the parent of the {@code ydsu} upper package.
     */
    private String classPathRoot

    ModuleMetadata(File homeDir) {
        extractModuleMetadata(homeDir)
    }

    String getSystemPathRoot() {
        systemPathRoot
    }

    ImmutableSet<String> getModuleNames() {
        moduleNames
    }

    String getClassPathRoot() {
        classPathRoot
    }

    private void extractModuleMetadata(File homeDir) {
        if (!homeDir.exists()) {
            throw new RuntimeException("Can't extract module metadata: directory does not exist: $homeDir")
        }
        if (homeDir.name == '.git') {
            throw new RuntimeException("Module's home directory cannot be named .git: $homeDir")
        }
        TreeSet<String> moduleNames = new TreeSet<>()
        /**
         * If the localPath directory has at least one regular file then it is a single module.
         * Otherwise, it's a collection of modules. Hidden files like .DS_Store are NOT taken into account.
         */
        if (homeDir.listFiles({ it.isFile() && !it.isHidden() } as FileFilter)) {
            systemPathRoot = homeDir.parent
            moduleNames << homeDir.name
            log.info 'Loaded single module {}', homeDir.name
        } else {
            systemPathRoot = homeDir.toString()
            homeDir.eachDir {
                moduleNames << it.name
            }
            log.info 'Loaded collection of modules {}', moduleNames
        }
        this.moduleNames = ImmutableSet.copyOf(moduleNames)
        classPathRoot = getClassPathRootFor(systemPathRoot)
    }

    private String getClassPathRootFor(String systemPathRoot) {
        /**
         * Each element in system path is always a single flattened module, never a collection of modules.
         * The systemPathRoot can be either a single module or a collection of modules. Either way, single or collection
         * of modules, all the classes under this directory will share a common classpath and a common upper package
         * holder directory.
         * We need to find the parent directory of the root package. That is where the classpath starts.
         * For example, parentOfRootPackage for this class will be the src/main/groovy directory.
         */
        String parentOfRootPackage = findParentOfRootPackage(new File(systemPathRoot))
        // If parent of root package was found and is parent of systemPathRoot
        if (parentOfRootPackage && systemPathRoot.startsWith(parentOfRootPackage)) {
            return parentOfRootPackage
        }
        systemPathRoot
    }

    private String findParentOfRootPackage(File systemPathRoot) {
        List<File> srcFiles = new ArrayList<>()
        systemPathRoot.eachFileRecurse(FileType.FILES) {
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
                return parentOfRootPackage.toString()
            }
        }
        return null
    }

    @Override
    String toString() {
        "ModuleMetadata{" +
                "systemPathRoot='" + systemPathRoot + '\'' +
                ", moduleNames=" + moduleNames +
                ", classPathRoot='" + classPathRoot + '\'' +
                '}'
    }
}
