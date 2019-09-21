package ydsu.module.util.source

import groovy.util.logging.Slf4j

@Slf4j
class ModuleFactory {
    private static ModuleConfFactory moduleConfFactory = new ModuleConfFactory()
    @Lazy
    private static volatile Module currentModule = {
        File systemPath = SystemPathUtil.scriptFile.parentFile
        new Module(systemPath.name, moduleConfFactory.newModuleConf(systemPath.toString()))
    }()

    /**
     * Get the {@link Module} instance of the current script in execution. Note that this method is specifically
     * designed to be invoked from within shell scripts. Directly invoking the method from Groovy code might result in
     * mis-mapping of the fields from the file system path. For example, when calling this method from within a shell
     * script located at /ydsu-module/src/main/groovy/mymodule, the 'mymodule' will be extracted as the module name. On
     * the other hand, when executing the same code from within a unit test the path will look more like
     * /ydsu-module/build/classes/groovy and will result in 'groovy' being interpreted as the module's name.
     *
     * @return a singleton {@link Module} instance for the current execution
     */
    static Module currentModule() {
        currentModule
    }
}
