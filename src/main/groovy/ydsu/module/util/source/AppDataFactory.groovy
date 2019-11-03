package ydsu.module.util.source

class AppDataFactory {
    //region ReadAppData
    /**
     * Create a new {@link ReadAppData} instance for the module of the current script in execution (aka current module).
     *
     * @return a new {@link ReadAppData} instance
     */
    static ReadAppData newReadAppData() {
        newReadAppData(CurrentModule.instance.id, CurrentModule.instance.systemPath)
    }

    /**
     * Create a new {@link ReadAppData} instance representing global cross-module shared resources. Global means that a
     * call for a particular resource will resolve to the same file, regardless of the module that is going to use it.
     *
     * @param moduleSystemPath the module's system path directory, used to fetch the default data
     * @return a new {@link ReadAppData} instance representing global cross-module shared resources
     */
    static ReadAppData newGlobalReadAppData(String moduleSystemPath = CurrentModule.instance.systemPath) {
        new ReadAppData(null, moduleSystemPath)
    }

    /**
     * Create a new {@link ReadAppData} instance for the given {@code moduleId} and {@code moduleSystemPath}. The module
     * id is used to fetch the overridden data and will separate the data from one module to another. The module system
     * path is used to fetch the default data.
     *
     * @param moduleId module id in the format of {@link CurrentModule#getId()} ("${owner}-${repository}-${name}") or
     *                 any arbitrary format, used to fetch the overridden data
     * @param moduleSystemPath the module's system path, used to fetch the default data
     * @return a new {@link ReadAppData} instance
     */
    static ReadAppData newReadAppData(String moduleId, String moduleSystemPath = CurrentModule.instance.systemPath) {
        new ReadAppData(moduleId, moduleSystemPath)
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
        newWriteAppData(CurrentModule.instance.id)
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
     * @param moduleId module id in the format of {@link CurrentModule#getId()} ("${owner}-${repository}-${name}") or
     *                 any arbitrary format, used to separate the data from one module to another
     * @return a new {@link WriteAppData} instance
     */
    static WriteAppData newWriteAppData(String moduleId) {
        new WriteAppData(moduleId)
    }
    //endregion
}
