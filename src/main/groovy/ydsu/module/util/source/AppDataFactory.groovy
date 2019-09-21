package ydsu.module.util.source

class AppDataFactory {
    @Lazy
    private static volatile Module currentModule = { ModuleFactory.currentModule() }()

    //region ReadAppData
    /**
     * Create a new {@link ReadAppData} instance for the module of the current script in execution (aka current module).
     * The id of the current module will be used to separate the data from other modules.
     *
     * @return a new {@link ReadAppData} instance
     */
    static ReadAppData newReadAppData() {
        newReadAppData(currentModule.id)
    }

    /**
     * Create a new {@link ReadAppData} instance representing global cross-module shared resources. Global means that a
     * call for a particular resource will resolve to the same file, regardless of the module that is going to use it.
     *
     * @return a new {@link ReadAppData} instance representing global cross-module shared resources
     */
    static ReadAppData newGlobalReadAppData() {
        new ReadAppData()
    }

    /**
     * Create a new {@link ReadAppData} instance for the given {@code moduleId}. The module id will separate the data
     * from one module to another.
     *
     * @param moduleId module id in the format of {@link Module#getId()} ("${owner}-${repository}-${name}") or any
     *                 arbitrary format
     * @return a new {@link ReadAppData} instance
     */
    static ReadAppData newReadAppData(String moduleId) {
        new ReadAppData(moduleId)
    }
    //endregion

    //region WriteAppData
    /**
     * Create a new {@link WriteAppData} instance for the module of the current script in execution (aka current
     * module). The id of the current module will be used to separate the data from other modules.
     *
     * @return a new {@link WriteAppData} instance
     */
    static WriteAppData newWriteAppData() {
        newWriteAppData(currentModule.id)
    }

    /**
     * Create a new {@link WriteAppData} instance representing global cross-module shared resources. Global means that a
     * call for a particular resource will resolve to the same file, regardless of the module that is going to use it.
     *
     * @return a new {@link WriteAppData} instance representing global cross-module shared resources
     */
    static WriteAppData newGlobalWriteAppData() {
        new WriteAppData()
    }

    /**
     * Create a new {@link WriteAppData} instance for the given {@code moduleId}. The module id will separate the data
     * from one module to another.
     *
     * @param moduleId module id in the format of {@link Module#getId()} ("${owner}-${repository}-${name}") or any
     *                 arbitrary format
     * @return a new {@link WriteAppData} instance
     */
    static WriteAppData newWriteAppData(String moduleId) {
        new WriteAppData(moduleId)
    }
    //endregion
}
