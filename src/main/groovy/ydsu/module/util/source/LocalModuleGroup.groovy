package ydsu.module.util.source

import com.google.common.collect.ImmutableSet
import groovy.transform.EqualsAndHashCode

import java.util.concurrent.ConcurrentHashMap
import java.util.function.Predicate

import static ydsu.module.util.source.Args.notNull

@Grab(group = 'com.google.guava', module = 'guava', version = '28.1-jre', transitive = false)
@EqualsAndHashCode
class LocalModuleGroup {
    private Predicate<File> baseDirContainsSingleModule
    private String confLine // https://github.com/yesday/ydsu-module/master/src/main/groovy/ydsu/module
    private String owner // yesday
    private String repository // ydsu-module
    private String relativePath // src/main/groovy/ydsu/module
    // /home/user/appdata/ydsu/cache/yesday-ydsu-manage/source/yesday/ydsu-module/src/main/groovy/ydsu/module
    private File baseDir
    private ConcurrentHashMap<String, Object> lazy = new ConcurrentHashMap<>()

    LocalModuleGroup(Predicate<File> baseDirContainsSingleModule, String confLine, String owner, String repository,
                     String relativePath, File baseDir) {
        notNull 'baseDir', baseDir
        this.baseDirContainsSingleModule = baseDirContainsSingleModule
        this.confLine = confLine
        this.owner = owner
        this.repository = repository
        this.relativePath = relativePath
        this.baseDir = baseDir
    }

    Predicate<File> getBaseDirContainsSingleModule() {
        baseDirContainsSingleModule
    }

    String getConfLine() {
        confLine
    }

    String getOwner() {
        owner
    }

    String getRepository() {
        repository
    }

    String getRelativePath() {
        relativePath
    }

    /**
     * Returns the base directory of the module on the local file system, for example,
     * {@code ~/my-ydsu-module/src/main/groovy/ydsu/module/mymodules} or
     * {@code ${PublicModuleGroup#sourceDir}/yesday/ydsu-module/src/main/groovy/ydsu/module}
     */
    File getBaseDir() {
        baseDir
    }

    boolean exists() {
        baseDir.exists()
    }

    ModuleGroupSystemPath getModuleGroupSystemPath() {
        lazy.computeIfAbsent('moduleGroupSystemPath', {
            exists() ? new ModuleGroupSystemPath(baseDirContainsSingleModule, baseDir) : null
        }) as ModuleGroupSystemPath
    }

    ImmutableSet<String> getSystemPath() {
        if (moduleGroupSystemPath) {
            TreeSet<String> systemPath = new TreeSet<>()
            for (String moduleName : moduleGroupSystemPath.moduleNames) {
                systemPath << "${moduleGroupSystemPath.systemPathRoot}/$moduleName".toString()
            }
            return ImmutableSet.copyOf(systemPath)
        }
        ImmutableSet.of()
    }

    ImmutableSet<String> getModuleIds() {
        if (moduleGroupSystemPath) {
            TreeSet<String> ids = new TreeSet<>()
            for (String moduleName : moduleGroupSystemPath.moduleNames) {
                ids << "${owner}-${repository}-$moduleName".toString()
            }
            return ImmutableSet.copyOf(ids)
        }
        ImmutableSet.of()
    }

    @Override
    String toString() {
        // Lazily initialised members (moduleGroupSystemPath, systemPath, moduleIds) are deliberately excluded
        "LocalModuleGroup{" +
                "baseDirContainsSingleModule=" + baseDirContainsSingleModule +
                ", confLine='" + confLine + '\'' +
                ", owner='" + owner + '\'' +
                ", repository='" + repository + '\'' +
                ", relativePath='" + relativePath + '\'' +
                ", baseDir=" + baseDir +
                '}'
    }
}
