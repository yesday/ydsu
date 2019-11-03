package ydsu.module.util.source

import groovy.transform.EqualsAndHashCode

import java.util.function.Predicate

import static ydsu.module.util.source.Exec.command

@EqualsAndHashCode
class PublicModuleGroup extends LocalModuleGroup {
    private boolean internal
    private String sourceDir
    private String httpsCloneUrl // https://github.com/yesday/ydsu-module.git
    private String branch // master

    PublicModuleGroup(Predicate<File> baseDirContainsSingleModule, boolean internal, String sourceDir, String confLine,
                      String owner, String repository, String relativePath, File baseDir, String httpsCloneUrl,
                      String branch) {
        super(baseDirContainsSingleModule, confLine, owner, repository, relativePath, baseDir)
        this.internal = internal
        this.sourceDir = sourceDir
        this.httpsCloneUrl = httpsCloneUrl
        this.branch = branch
    }

    boolean isInternal() {
        return internal
    }

    String getSourceDir() {
        sourceDir
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
        if (exists()) {
            throw new RuntimeException("Module group's repository has been already cloned: " +
                    "module group's base directory exists: $baseDir")
        }
        File ownerDir = new File(sourceDir, owner)
        File repoDir = new File(ownerDir, repository)
        if (repoDir.exists()) {
            throw new RuntimeException("Module group's repository has been already cloned: " +
                    "repository directory exists: $repoDir")
        }
        ownerDir.mkdirs()
        String cmd = "GIT_TERMINAL_PROMPT=0 git clone -b $branch $httpsCloneUrl"
        command(cmd, ownerDir)
        if (!exists()) {
            throw new RuntimeException("Module group's relative path does not exist: $relativePath" +
                    ": module group's base directory wasn't found after clone: $baseDir")
        }
    }

    @Override
    String toString() {
        "PublicModuleGroup{" +
                "baseDirContainsSingleModule=" + baseDirContainsSingleModule +
                ", internal=" + internal +
                ", sourceDir='" + sourceDir + '\'' +
                ", confLine='" + confLine + '\'' +
                ", owner='" + owner + '\'' +
                ", repository='" + repository + '\'' +
                ", relativePath='" + relativePath + '\'' +
                ", baseDir=" + baseDir +
                ", httpsCloneUrl='" + httpsCloneUrl + '\'' +
                ", branch='" + branch + '\'' +
                '}'
    }
}
