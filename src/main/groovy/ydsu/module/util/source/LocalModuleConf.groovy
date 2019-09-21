package ydsu.module.util.source

import com.google.common.collect.ImmutableSet
import groovy.transform.EqualsAndHashCode

import java.util.concurrent.ConcurrentHashMap

@Grab(group = 'com.google.guava', module = 'guava', version = '28.1-jre', transitive = false)
@EqualsAndHashCode
class LocalModuleConf {
    private String confLine // https://github.com/yesday/ydsu-module/master/src/main/groovy/ydsu/module
    private String owner // yesday
    private String repository // ydsu-module
    private String relativePath // src/main/groovy/ydsu/module
    private File homeDir
    private ConcurrentHashMap<String, Object> lazy = new ConcurrentHashMap<>()

    LocalModuleConf(String confLine, String owner, String repository, String relativePath, File homeDir) {
        this.confLine = confLine
        this.owner = owner
        this.repository = repository
        this.relativePath = relativePath
        this.homeDir = homeDir
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
     * Returns the home directory of the module on the local file system, for example,
     * {@code ~/my-ydsu-module/src/main/groovy/ydsu/module/mymodule}
     */
    File getHomeDir() {
        homeDir
    }

    ModuleMetadata getModuleMetadata() {
        lazy.computeIfAbsent('moduleMetadata', { getHomeDir()?.exists() ? new ModuleMetadata(getHomeDir()) : null })
    }

    ImmutableSet<String> getSystemPath() {
        if (moduleMetadata) {
            TreeSet<String> systemPath = new TreeSet<>()
            for (String moduleName : moduleMetadata.moduleNames) {
                systemPath << "${moduleMetadata.systemPathRoot}/$moduleName"
            }
            return ImmutableSet.copyOf(systemPath)
        }
        ImmutableSet.of()
    }

    ImmutableSet<String> getClassPath() {
        if (moduleMetadata) {
            TreeSet<String> classPath = new TreeSet<>()
            classPath << moduleMetadata.classPathRoot
            new File(moduleMetadata.classPathRoot).eachDirRecurse {
                classPath << it.toString()
            }
            return ImmutableSet.copyOf(classPath)
        }
        ImmutableSet.of()
    }

    @Override
    String toString() {
        "LocalModuleConf{" +
                "confLine='" + confLine + '\'' +
                ", owner='" + owner + '\'' +
                ", repository='" + repository + '\'' +
                ", relativePath='" + relativePath + '\'' +
                ", homeDir=" + homeDir +
                ", moduleMetadata=" + moduleMetadata +
                ", systemPath=" + systemPath +
                ", classPath=" + classPath +
                '}'
    }
}
