package ydsu.module.util.source

import groovy.transform.EqualsAndHashCode
import ydsu.module.util.conf.AppDataDirConf

import static ydsu.module.util.source.Exec.command

@EqualsAndHashCode
class PublicModuleConf extends LocalModuleConf {
    public static final String MODULE_SOURCE = "${AppDataDirConf.CACHE}source/"
    private String httpsCloneUrl // https://github.com/yesday/ydsu-module.git
    private String branch // master

    PublicModuleConf(String confLine, String owner, String repository, String relativePath, String httpsCloneUrl,
                     String branch) {
        super(confLine, owner, repository, relativePath, null)
        this.httpsCloneUrl = httpsCloneUrl
        this.branch = branch
    }

    /**
     * Returns the home directory of the module on the local file system, for example,
     * {@code ${AppDataDirConf.CACHE}/yesday/ydsu-module/src/main/groovy/ydsu/module}
     */
    @Override
    File getHomeDir() {
        new File("${MODULE_SOURCE}/${owner}/${repository}/${relativePath}")
    }

    String getHttpsCloneUrl() {
        httpsCloneUrl
    }

    String getBranch() {
        branch
    }

    /**
     * Clones the git repository.
     */
    void gitClone() {
        if (homeDir.exists()) {
            throw new RuntimeException("Module's repository has been already cloned: " +
                    "module's home directory exists: $homeDir")
        }
        File ownerDir = new File("${MODULE_SOURCE}/${owner}")
        File repoDir = new File(ownerDir, repository)
        if (repoDir.exists()) {
            throw new RuntimeException("Module's repository has been already cloned: " +
                    "repository directory exists: $repoDir")
        }
        ownerDir.mkdirs()
        String cmd = "GIT_TERMINAL_PROMPT=0 git clone -b $branch $httpsCloneUrl"
        command(cmd, ownerDir)
        if (!homeDir.exists()) {
            throw new RuntimeException("Module's relative path does not exist: $relativePath" +
                    ": module's home directory wasn't found after clone: $homeDir")
        }
    }

    @Override
    String toString() {
        "PublicModuleConf{" +
                "confLine='" + confLine + '\'' +
                ", owner='" + owner + '\'' +
                ", repository='" + repository + '\'' +
                ", relativePath='" + relativePath + '\'' +
                ", homeDir=" + homeDir +
                ", moduleMetadata=" + moduleMetadata +
                ", systemPath=" + systemPath +
                ", classPath=" + classPath +
                ", httpsCloneUrl='" + httpsCloneUrl + '\'' +
                ", branch='" + branch + '\'' +
                '}'
    }
}
