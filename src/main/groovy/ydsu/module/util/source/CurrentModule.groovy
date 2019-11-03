package ydsu.module.util.source

import groovy.transform.EqualsAndHashCode

/**
 * A singleton {@link CurrentModule} instance for the current execution. Note that this class is specifically designed
 * to be instantiated from within shell scripts. Directly instantiating the class from Groovy code might result in
 * mis-mapping of the module's attributes from the file system path. For example, when instantiating this class from
 * within a shell script located at /ydsu-module/src/main/groovy/mymodule, the 'mymodule' will be extracted as the
 * module name. On the other hand, when executing the same code from within a unit test the path will look more like
 * /ydsu-module/build/classes/groovy and will result in 'groovy' being interpreted as the module's name.
 */
@Singleton(lazy = true, strict = false)
@EqualsAndHashCode
class CurrentModule {
    private String id
    private String name
    private String systemPath
    private File systemPathFile
    private LocalModuleGroup localModuleGroup

    CurrentModule() {
        localModuleGroup = new SystemPathModuleGroupFactory().newModuleGroup(SystemPathUtil.scriptFile.parentFile)
        id = localModuleGroup.moduleIds[0]
        name = localModuleGroup.moduleGroupSystemPath.moduleNames[0]
        systemPath = localModuleGroup.systemPath[0] // = SystemPathUtil.scriptFile.parent
        systemPathFile = systemPath as File // = SystemPathUtil.scriptFile.parentFile
    }

    String getId() {
        id
    }

    String getName() {
        name
    }

    String getSystemPath() {
        systemPath
    }

    File getSystemPathFile() {
        systemPathFile
    }

    LocalModuleGroup getLocalModuleGroup() {
        localModuleGroup
    }

    @Override
    String toString() {
        "CurrentModule{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", systemPath='" + systemPath + '\'' +
                ", localModuleGroup=" + localModuleGroup +
                '}'
    }
}
