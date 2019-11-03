package ydsu.module.util.source

import com.google.common.collect.ImmutableSet
import groovy.transform.EqualsAndHashCode
import groovy.util.logging.Slf4j

import java.util.function.Predicate

import static ydsu.module.util.source.Args.notNull
import static ydsu.module.util.source.Args.systemPath

@Slf4j
@EqualsAndHashCode
class ModuleGroupSystemPath {
    private String systemPathRoot
    private ImmutableSet<String> moduleNames

    ModuleGroupSystemPath(Predicate<File> baseDirContainsSingleModule, File baseDir) {
        splitBaseDirToModules(baseDirContainsSingleModule, baseDir)
    }

    String getSystemPathRoot() {
        systemPathRoot
    }

    ImmutableSet<String> getModuleNames() {
        moduleNames
    }

    private void splitBaseDirToModules(Predicate<File> baseDirContainsSingleModule, File baseDir) {
        notNull 'baseDirContainsSingleModule', baseDirContainsSingleModule
        systemPath 'baseDir', baseDir
        TreeSet<String> moduleNames = new TreeSet<>()
        if (baseDirContainsSingleModule.test(baseDir)) {
            systemPathRoot = baseDir.parent
            moduleNames << baseDir.name
            log.info 'Loaded single module {}', baseDir.name
        } else {
            systemPathRoot = baseDir.toString()
            baseDir.eachDir {
                moduleNames << it.name
            }
            log.info 'Loaded collection of modules {}', moduleNames
        }
        this.moduleNames = ImmutableSet.copyOf(moduleNames)
    }

    @Override
    String toString() {
        "ModuleGroupSystemPath{" +
                "systemPathRoot='" + systemPathRoot + '\'' +
                ", moduleNames=" + moduleNames +
                '}'
    }
}
