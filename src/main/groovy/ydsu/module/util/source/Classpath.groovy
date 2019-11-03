package ydsu.module.util.source

import com.google.common.collect.ImmutableSet
import com.google.common.io.Files
import groovy.io.FileType

import java.util.regex.Matcher
import java.util.regex.Pattern

@Grab(group = 'com.google.guava', module = 'guava', version = '28.1-jre', transitive = false)
class Classpath {
    private static final String PACKAGE_REGEX = /^package (.+)/
    private static final Pattern PACKAGE_PATTERN = Pattern.compile(PACKAGE_REGEX)

    static ImmutableSet<String> from(String systemPath) {
        String tidied = Args.systemPath('systemPath', systemPath)
        TreeSet<String> classpath = new TreeSet<>()
        String classpathRoot = findClasspathRootFor(tidied)
        classpath << classpathRoot
        new File(classpathRoot).eachDirRecurse {
            classpath << it.toString()
        }
        ImmutableSet.copyOf(classpath)
    }

    static String findClasspathRootFor(String systemPath) {
        String tidied = Args.systemPath('systemPath', systemPath)
        /**
         * Each element in system path is always a single flattened module, never a collection of modules. The
         * systemPathRoot can be either a single module or a collection of modules. Either way, single or collection of
         * modules, all the classes under this directory will share a common classpath and a common upper package holder
         * directory.
         * We need to find the parent directory of the root package. That is where the classpath starts.
         * For example, parentDirOfRootPackage for this class will be the src/main/groovy directory.
         */
        String parentDirOfRootPackage = findParentDirOfRootPackage(new File(tidied))
        // If parent of root package was found and is parent of systemPathRoot
        if (parentDirOfRootPackage && systemPath.startsWith(parentDirOfRootPackage)) {
            return parentDirOfRootPackage
        }
        systemPath
    }

    private static String findParentDirOfRootPackage(File systemPath) {
        List<File> srcFiles = new ArrayList<>()
        systemPath.eachFileRecurse(FileType.FILES) {
            if (['java', 'groovy', 'kt'].contains(Files.getFileExtension(it.name))) {
                srcFiles << it
            }
        }
        for (File it : srcFiles) {
            Matcher m = PACKAGE_PATTERN.matcher(it.text)
            if (m.find()) {
                String pkg = it.text.substring(m.start() + 8, m.end())
                int count = pkg.count('.') + 1
                File parentDirOfRootPackage = it.parentFile
                count.times {
                    parentDirOfRootPackage = parentDirOfRootPackage.parentFile
                }
                return parentDirOfRootPackage.toString()
            }
        }
        return null
    }
}
