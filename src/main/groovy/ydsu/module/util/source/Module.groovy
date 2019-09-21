package ydsu.module.util.source

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class Module {
    private String name
    private LocalModuleConf localModuleConf

    Module(String name, LocalModuleConf localModuleConf) {
        this.name = name
        this.localModuleConf = localModuleConf
    }

    String getName() {
        name
    }

    LocalModuleConf getLocalModuleConf() {
        localModuleConf
    }

    String getId() {
        "${localModuleConf.owner}-${localModuleConf.repository}-${name}"
    }

    String getSystemPath() {
        localModuleConf.moduleMetadata ? "${localModuleConf.moduleMetadata.systemPathRoot}/${name}" : null
    }

    String getClassPathRoot() {
        localModuleConf.moduleMetadata?.classPathRoot
    }

    @Override
    String toString() {
        "Module{" +
                "name='" + name + '\'' +
                ", localModuleConf=" + localModuleConf +
                ", id='" + id + '\'' +
                ", systemPath='" + systemPath + '\'' +
                ", classPathRoot='" + classPathRoot + '\'' +
                '}'
    }
}
