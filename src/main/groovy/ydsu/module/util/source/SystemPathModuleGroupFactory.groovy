package ydsu.module.util.source

import ydsu.module.util.conf.AppDataDirConf

import javax.validation.constraints.NotNull

import static ydsu.module.util.source.Args.notBlank
import static ydsu.module.util.source.ModuleAttribute.*

/**
 * Factory that can create module group instances from system path.
 *
 * @see ConfLineModuleGroupFactory
 */
@Grab(group = 'javax.validation', module = 'validation-api', version = '2.0.1.Final', transitive = false)
class SystemPathModuleGroupFactory {
    private String publicModulesSourceDir
    private ModuleAttributeParser moduleAttributeParser

    /**
     * Constructs a factory that can create module group instances from system path.
     *
     * @param publicModulesSourceDir the source directory where public modules get cloned to
     */
    SystemPathModuleGroupFactory(@NotNull String publicModulesSourceDir = AppDataDirConf.PUBLIC_MODULES_SOURCE) {
        notBlank 'publicModulesSourceDir', publicModulesSourceDir
        this.publicModulesSourceDir = publicModulesSourceDir
        moduleAttributeParser = new ModuleAttributeParser(publicModulesSourceDir)
    }

    /**
     * Create a new module group instance from the given {@code systemPath}. As opposite to {@link
     * ConfLineModuleGroupFactory#newModuleGroup(java.lang.String)}, a module group instance created using this factory
     * always represents a single module rather than a collection of modules.
     *
     * If the system path resides inside {@link #publicModulesSourceDir} then returns a {@link PublicModuleGroup},
     * otherwise a {@link LocalModuleGroup}, unless the system path resides inside {@link
     * AppDataDirConf#INTERNAL_MODULES_REPOSITORY}. Both types ({@link PublicModuleGroup} and {@link LocalModuleGroup})
     * always represent a single module and not a collection of modules, which is automatically determined by using the
     * {@link AlwaysTrue} predicate singleton. Also, if the system path resides inside {@link
     * AppDataDirConf#INTERNAL_MODULES_REPOSITORY} the flag {@link PublicModuleGroup#internal} is set to {@code true}
     * and the module is an 'internal' public module.
     *
     * @param systemPath system path defaulting to the parent directory of the current script in execution
     * @return new instance of {@link LocalModuleGroup} or its subclass {@link PublicModuleGroup}
     * @throws IllegalArgumentException When {@code systemPath} is null or does not exist or is not a directory
     */
    @NotNull
    LocalModuleGroup newModuleGroup(File systemPath = CurrentModule.instance.systemPathFile) {
        Args.systemPath('systemPath', systemPath)
        EnumMap<ModuleAttribute, String> attribute
        try {
            attribute = moduleAttributeParser.parse(systemPath.toString())
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not parse systemPath: ${systemPath.toString()}", e)
        }
        if (attribute[PUBLIC] == 'true') {
            return new PublicModuleGroup(AlwaysTrue.instance, attribute[INTERNAL] == 'true', attribute[SOURCE_DIR],
                    null, attribute[OWNER], attribute[REPOSITORY], attribute[RELATIVE_PATH],
                    attribute[BASE_DIR] as File, attribute[HTTPS_CLONE_URL], attribute[BRANCH])
        }
        new LocalModuleGroup(AlwaysTrue.instance, null, attribute[OWNER], attribute[REPOSITORY],
                attribute[RELATIVE_PATH], attribute[BASE_DIR] as File)
    }
}
