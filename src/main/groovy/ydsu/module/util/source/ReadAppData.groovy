package ydsu.module.util.source

import groovy.transform.EqualsAndHashCode
import ydsu.module.util.conf.AppDataDirConf

import javax.validation.constraints.NotBlank
import java.nio.file.Paths

/**
 * Application data class used for reading purposes.
 *
 * Application data can be found in the following locations:
 *
 * <ul>
 *  <li>Default - The default application data is stored inside the module's home directory. For example, if the default
 *  location is used, a call to {@code conf( '.' )} will resolve to the 'conf' subdirectory relative to the path of the
 *  current script in execution.</li>
 *  <li>Overridden - The overridden location takes precedence over the default and resides inside the {@link
 *  AppDataDirConf#APP_HOME} directory. For example, if a configuration file exists in both locations then the
 *  overridden takes effect. The class {@link WriteAppData} is in fact a facade representing the Overridden location for
 *  the application data.</li>
 *  <li>Effective - This is the location that takes effect for a given resource. It resolves to either the overridden or
 *  default whichever comes first, in that order. The class {@link ReadAppData} is in fact a facade representing the
 *  Effective location for the application data.</li>
 * </ul>
 */
@Grab(group = 'javax.validation', module = 'validation-api', version = '2.0.1.Final')
@EqualsAndHashCode
class ReadAppData {
    private String moduleId
    private String moduleHomeDir

    /**
     * Create a new instance for the given {@code moduleId}. The module id will separate the data from one module to
     * another. If module id is {@code null} then the instance will represent global cross-module shared resources. The
     * latter means that a call for a particular resource will resolve to the same file, regardless of the module that
     * is going to use it.
     *
     * @param moduleId the module id or {@code null} for a global cross-module shared instance
     */
    ReadAppData(String moduleId = null) {
        this.moduleId = moduleId
        moduleHomeDir = SystemPathUtil.scriptFile.parent
    }

    File source(@NotBlank String path) {
        getEffectiveResource(AppDataDirConf.SOURCE, 'source', path)
    }

    File build(@NotBlank String path) {
        getEffectiveResource(AppDataDirConf.BUILD, 'build', path)
    }

    File conf(@NotBlank String path) {
        getEffectiveResource(AppDataDirConf.CONF, 'conf', path)
    }

    File secret(@NotBlank String path) {
        getEffectiveResource(AppDataDirConf.SECRET, 'secret', path)
    }

    File log(@NotBlank String path) {
        getEffectiveResource(AppDataDirConf.LOG, 'log', path)
    }

    File data(@NotBlank String path) {
        getEffectiveResource(AppDataDirConf.DATA, 'data', path)
    }

    File cache(@NotBlank String path) {
        getEffectiveResource(AppDataDirConf.CACHE, 'cache', path)
    }

    File doc(@NotBlank String path) {
        getEffectiveResource(AppDataDirConf.DOC, 'doc', path)
    }

    File report(@NotBlank String path) {
        getEffectiveResource(AppDataDirConf.REPORT, 'report', path)
    }

    /**
     * Get the resource file that should take effect for the given appdata {@code type}.
     *
     * @param appDataDirConf the parent appdata directory holding overridden data
     * @param type the type of the application data such as source, build, etc
     * @param path the relative path to the resource
     * @return the overridden or default file whichever exists in that order
     */
    private File getEffectiveResource(String appDataDirConf, String type, String path) {
        Args.notBlank('path', path)
        File theDefault = Paths.get(moduleHomeDir, type, path).toFile()
        File overridden
        if (moduleId) {
            overridden = Paths.get(appDataDirConf, moduleId, path).toFile()
        } else {
            overridden = Paths.get(appDataDirConf, path).toFile()
        }
        !overridden.exists() && theDefault.exists() ? theDefault : overridden
    }

    @Override
    String toString() {
        "ReadAppData{" +
                "moduleId='" + moduleId + '\'' +
                ", moduleHomeDir='" + moduleHomeDir + '\'' +
                ", source=" + source('.') +
                ", build=" + build('.') +
                ", conf=" + conf('.') +
                ", secret=" + secret('.') +
                ", log=" + log('.') +
                ", data=" + data('.') +
                ", cache=" + cache('.') +
                ", doc=" + doc('.') +
                ", report=" + report('.') +
                '}'
    }
}
