package ydsu.module.util.source

import ydsu.module.util.conf.AppDataDirConf

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import java.util.function.Predicate

import static ydsu.module.util.source.Args.notBlank
import static ydsu.module.util.source.Args.notNull
import static ydsu.module.util.source.ModuleAttribute.*

/**
 * Factory that can create module group instances from configuration line (confLine argument).
 *
 * @see SystemPathModuleGroupFactory
 */
@Grab(group = 'javax.validation', module = 'validation-api', version = '2.0.1.Final', transitive = false)
class ConfLineModuleGroupFactory {
    private Predicate<File> baseDirContainsSingleModule
    private String publicModulesSourceDir
    private ModuleAttributeParser moduleAttributeParser

    /**
     * Constructs a factory that can create module group instances from configuration line (confLine argument).
     *
     * @param baseDirContainsSingleModule predicate determining whether a given base directory contains a single module
     *                                    or a collection of modules
     * @param publicModulesSourceDir the source directory where public modules get cloned to
     */
    ConfLineModuleGroupFactory(@NotNull Predicate<File> baseDirContainsSingleModule = AlwaysTrue.instance,
                               @NotBlank String publicModulesSourceDir = AppDataDirConf.PUBLIC_MODULES_SOURCE) {
        notNull 'baseDirContainsSingleModule', baseDirContainsSingleModule
        notBlank 'publicModulesSourceDir', publicModulesSourceDir
        this.baseDirContainsSingleModule = baseDirContainsSingleModule
        this.publicModulesSourceDir = publicModulesSourceDir
        moduleAttributeParser = new ModuleAttributeParser(publicModulesSourceDir)
    }

    /**
     * Create a new module group instance from the given {@code confLine}. A module group can represent a single module
     * or a collection of modules.
     *
     * If the configuration ({@code confLine}), which is loosely speaking a URL or a file system path, starts with
     * https:// then returns a {@link PublicModuleGroup}, otherwise a {@link LocalModuleGroup}, unless the file system
     * path resides inside {@link #publicModulesSourceDir} or {@link AppDataDirConf#INTERNAL_MODULES_REPOSITORY}. Both
     * types ({@link PublicModuleGroup} and {@link LocalModuleGroup}) can represent either a single module or a
     * collection of modules, which is automatically determined by using the {@link #baseDirContainsSingleModule}
     * predicate. Also, if the file system path resides inside {@link AppDataDirConf#INTERNAL_MODULES_REPOSITORY} the
     * flag {@link PublicModuleGroup#internal} is set to {@code true} and the module is an 'internal' public module.
     *
     * @param confLine configuration line in public or local module format (loosely speaking a URL or a file system
     *                 path)
     * @return new instance of {@link LocalModuleGroup} or its subclass {@link PublicModuleGroup}
     * @throws IllegalArgumentException When {@code confLine} is blank or not formatted correctly
     */
    @NotNull
    LocalModuleGroup newModuleGroup(@NotBlank String confLine) {
        notBlank 'confLine', confLine
        EnumMap<ModuleAttribute, String> attribute
        try {
            attribute = moduleAttributeParser.parse(confLine)
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not parse confLine: $confLine", e)
        }
        if (attribute[PUBLIC] == 'true') {
            return new PublicModuleGroup(baseDirContainsSingleModule, attribute[INTERNAL] == 'true',
                    attribute[SOURCE_DIR], confLine, attribute[OWNER], attribute[REPOSITORY], attribute[RELATIVE_PATH],
                    attribute[BASE_DIR] as File, attribute[HTTPS_CLONE_URL], attribute[BRANCH])
        }
        new LocalModuleGroup(baseDirContainsSingleModule, confLine, attribute[OWNER], attribute[REPOSITORY],
                attribute[RELATIVE_PATH], attribute[BASE_DIR] as File)
    }
}
