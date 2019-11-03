package ydsu.module.util.source

import groovy.util.logging.Slf4j
import org.yaml.snakeyaml.Yaml

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import java.util.function.Predicate

import static ydsu.module.util.source.Args.notBlank
import static ydsu.module.util.source.Args.notNull

@Grab(group = 'org.yaml', module = 'snakeyaml', version = '1.25', transitive = false)
@Grab(group = 'javax.validation', module = 'validation-api', version = '2.0.1.Final', transitive = false)
@Slf4j
class ModuleLoader {
    //region constructor parameters
    private Predicate<File> baseDirContainsSingleModule
    private String appDataModuleId
    private String appDataModuleSystemPath
    private String confName
    //endregion
    //region inferred members
    private ReadAppData readAppData
    private WriteAppData writeAppData
    private File moduleReadConf
    private File moduleWriteConf
    private File publicModulesSourceDir
    //endregion

    ModuleLoader(@NotNull Predicate<File> baseDirContainsSingleModule = AlwaysTrue.instance,
                 @NotBlank String appDataModuleId = CurrentModule.instance.id,
                 @NotBlank String appDataModuleSystemPath = CurrentModule.instance.systemPath,
                 @NotBlank String confName = 'module') {
        notNull 'baseDirContainsSingleModule', baseDirContainsSingleModule
        notBlank 'appDataModuleId', appDataModuleId
        notBlank 'appDataModuleSystemPath', appDataModuleSystemPath
        notBlank 'confName', confName
        this.baseDirContainsSingleModule = baseDirContainsSingleModule
        this.appDataModuleId = appDataModuleId
        this.appDataModuleSystemPath = appDataModuleSystemPath
        this.confName = confName
        readAppData = AppDataFactory.newReadAppData(appDataModuleId, appDataModuleSystemPath)
        writeAppData = AppDataFactory.newWriteAppData(appDataModuleId)
        moduleReadConf = readAppData.conf("${confName}.yml")
        moduleWriteConf = writeAppData.conf("${confName}.yml")
        publicModulesSourceDir = writeAppData.cache('source/')
    }

    /**
     * Loads the changes from the `${confName}.yml` file and returns a list of corresponding {@link LocalModuleGroup}
     * objects. Git clones modules into `~/appdata/ydsu/cache` and/or removes the ones that are no longer present inside
     * the `${confName}.yml` file. Note that this method will not update existing modules. Updating existing modules is
     * outside of the scope of this method and can be implemented separately.
     *
     * @return list of {@link LocalModuleGroup} objects representing configuration inside `${confName}.yml` or empty
     *         list if no configuration was found
     */
    @NotNull
    List<LocalModuleGroup> loadModuleGroups() {
        List<String> confLines = parseModulesConfiguration()
        List<LocalModuleGroup> modules = createModuleGroups(confLines)
        cloneModules(modules)
        cleanCache(modules)
        modules
    }

    private List<String> parseModulesConfiguration() {
        if (moduleReadConf.exists()) {
            Yaml yaml = new Yaml()
            def conf = yaml.load(moduleReadConf.text)
            List<String> confLines
            if (conf instanceof String) {
                confLines = [conf]
            } else {
                if (conf[confName]) {
                    if (conf[confName] instanceof String) {
                        confLines = [conf[confName]]
                    } else {
                        confLines = conf[confName]
                    }
                } else {
                    throw new RuntimeException("Could not find property $confName in $moduleReadConf")
                }
            }
            if (confLines) {
                log.info "Using ${confName}.yml: ${moduleReadConf}"
                if (moduleReadConf != moduleWriteConf) {
                    log.info "To update the configuration create a copy of the above ${confName}.yml in ${moduleWriteConf}"
                }
                return confLines
            } else {
                log.warn "${confName} list is empty, no ${confName} is loaded: ${moduleReadConf}"
            }
        } else {
            log.warn "File does not exist, no ${confName} is loaded: ${moduleReadConf}"
        }
        []
    }

    private List<LocalModuleGroup> createModuleGroups(List<String> confLines) {
        ConfLineModuleGroupFactory confLineModuleFactory = new ConfLineModuleGroupFactory(baseDirContainsSingleModule, publicModulesSourceDir.toString())
        List<LocalModuleGroup> moduleGroups
        try {
            moduleGroups = confLines.collect { confLineModuleFactory.newModuleGroup(it) }
        } catch (IllegalArgumentException e) {
            String msg = "Could not parse ${moduleReadConf}: "
            if (moduleReadConf == moduleWriteConf) {
                msg += "fix the errors and try again"
            } else {
                msg += "create a copy in ${moduleWriteConf}, fix the errors, and try again"
            }
            throw new RuntimeException(msg, e)
        }
        moduleGroups
    }

    private void cloneModules(List<LocalModuleGroup> moduleGroups) {
        moduleGroups.each {
            if (it instanceof PublicModuleGroup) {
                if (!it.exists()) {
                    PublicModuleGroup pm = it
                    pm.gitClone()
                }
            } else {
                if (!it.exists()) {
                    log.warn "Local ${confName} not found: ${it.confLine}"
                }
            }
        }
    }

    private void cleanCache(List<LocalModuleGroup> moduleGroups) {
        Set<String> owners = moduleGroups.collect { it.owner }
        Set<String> repositories = moduleGroups.collect { "${it.owner}/${it.repository}".toString() }
        if (publicModulesSourceDir.exists()) {
            publicModulesSourceDir.eachDir {
                if (owners.contains(it.name)) {
                    it.eachDir {
                        String repo = "${it.parentFile.name}/${it.name}"
                        if (!repositories.contains(repo)) {
                            if (it.deleteDir()) {
                                log.info "Repository '${repo}' is no longer in use inside ${moduleReadConf}: " +
                                        "directory deleted: ${it.toString()}"
                            } else {
                                log.error "Repository '${repo}' is no longer in use inside ${moduleReadConf}: " +
                                        "however directory could not be deleted: ${it.toString()}"
                            }
                        }
                    }
                } else {
                    if (it.deleteDir()) {
                        log.info "Repository owner '${it.name}' is no longer in use inside ${moduleReadConf}: " +
                                "directory deleted: ${it.toString()}"
                    } else {
                        log.error "Repository owner '${it.name}' is no longer in use inside ${moduleReadConf}: " +
                                "however directory could not be deleted: ${it.toString()}"
                    }
                }
            }
        }
    }
}
