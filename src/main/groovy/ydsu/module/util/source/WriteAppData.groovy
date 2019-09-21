package ydsu.module.util.source

import groovy.transform.EqualsAndHashCode
import ydsu.module.util.conf.AppDataDirConf

import javax.validation.constraints.NotBlank
import java.nio.file.Paths

/**
 * Application data class used for writing purposes.
 *
 * The class {@link WriteAppData} is in fact a facade representing the Overridden location for the application data.
 * Refer to the documentation of {@link ReadAppData} for an explanation of what the Overridden location is.
 */
@Grab(group = 'javax.validation', module = 'validation-api', version = '2.0.1.Final')
@EqualsAndHashCode
class WriteAppData {
    private String moduleId

    /**
     * Create a new instance for the given {@code moduleId}. The module id will separate the data from one module to
     * another. If module id is {@code null} then the instance will represent global cross-module shared resources. The
     * latter means that a call for a particular resource will resolve to the same file, regardless of the module that
     * is going to use it.
     *
     * @param moduleId the module id or {@code null} for a global cross-module shared instance
     */
    WriteAppData(String moduleId = null) {
        this.moduleId = moduleId
    }

    File source(@NotBlank String path) {
        getOverriddenResource(AppDataDirConf.SOURCE, path)
    }

    File build(@NotBlank String path) {
        getOverriddenResource(AppDataDirConf.BUILD, path)
    }

    File conf(@NotBlank String path) {
        getOverriddenResource(AppDataDirConf.CONF, path)
    }

    File secret(@NotBlank String path) {
        getOverriddenResource(AppDataDirConf.SECRET, path)
    }

    File log(@NotBlank String path) {
        getOverriddenResource(AppDataDirConf.LOG, path)
    }

    File data(@NotBlank String path) {
        getOverriddenResource(AppDataDirConf.DATA, path)
    }

    File cache(@NotBlank String path) {
        getOverriddenResource(AppDataDirConf.CACHE, path)
    }

    File doc(@NotBlank String path) {
        getOverriddenResource(AppDataDirConf.DOC, path)
    }

    File report(@NotBlank String path) {
        getOverriddenResource(AppDataDirConf.REPORT, path)
    }

    /**
     * Get the overridden resource file for the given appdata {@code type}.
     *
     * @param appDataDirConf the parent appdata directory holding overridden data
     * @param path the relative path to the resource
     * @return the overridden file
     */
    private File getOverriddenResource(String appDataDirConf, String path) {
        Args.notBlank('path', path)
        File overridden = moduleId ? Paths.get(appDataDirConf, moduleId, path).toFile() : Paths.get(appDataDirConf, path).toFile()
        if (!overridden.parentFile.exists()) {
            overridden.parentFile.mkdirs()
        }
        overridden
    }

    @Override
    String toString() {
        "WriteAppData{" +
                "moduleId='" + moduleId + '\'' +
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
