package ydsu.module.util.source

import groovy.transform.PackageScope
import ydsu.module.util.conf.AppDataDirConf

class ModuleConfFactory {
    /**
     * Create a new module configuration object from the given {@code confLine}. A module configuration can refer to a
     * single module or a collection of modules.
     *
     * If the configuration (URL or file system path) starts with https:// then returns a {@link PublicModuleConf},
     * otherwise a {@link LocalModuleConf}. Both configurations ({@link PublicModuleConf} and {@link LocalModuleConf})
     * can refer to either a single module or a collection of modules.
     *
     * @param confLine configuration line in public or local module format
     * @return new instance of {@link LocalModuleConf} or {@link PublicModuleConf}
     * @throws IllegalArgumentException When {@code confLine} is not formatted correctly
     */
    LocalModuleConf newModuleConf(String confLine) {
        Args.notBlank('confLine', confLine)
        if (confLine == AppDataDirConf.USER_HOME) {
            throw new IllegalArgumentException("Argument 'confLine' cannot be equal to the user home directory")
        }
        if (confLine.startsWith('https://')) {
            return newPublicModuleConf(confLine)
        } else if (confLine.charAt(0) == '/') {
            return newLocalModuleConf(confLine)
        } else if (confLine.charAt(0) == '~') {
            return newLocalModuleConf(confLine)
        }
        String msg = """
Argument 'confLine' is not formatted correctly: $confLine: should start with https:// or / or ~: Examples:
- https://github.com/yesday/ydsu-module/master/src/main/groovy/ydsu/module/skeleton
- /full/path/to/another/local/modules-project/src/main/groovy/ydsu/module
- ~/some/local/modules-project/src/main/groovy/ydsu/module
"""
        throw new IllegalArgumentException(msg)
    }

    private PublicModuleConf newPublicModuleConf(String confLine) {
        int begin = 8 // https:// = 8 characters
        int end = endIndex(confLine, begin)
        String host = confLine.substring(begin, end)
        begin = end + 1
        end = endIndex(confLine, begin)
        String owner = confLine.substring(begin, end)
        begin = end + 1
        end = endIndex(confLine, begin)
        String repository = confLine.substring(begin, end)
        String httpsCloneUrl = "https://$host/${owner}/${repository}.git"
        begin = end + 1
        end = endIndex(confLine, begin)
        String branch = confLine.substring(begin, end)
        begin = end + 1
        String relativePath = confLine.substring(begin)
        new PublicModuleConf(confLine, owner, repository, relativePath, httpsCloneUrl, branch)
    }

    private int endIndex(String confLine, int begin) {
        int end = confLine.indexOf('/', begin)
        if (end < 0) {
            throw new IllegalArgumentException("Argument 'confLine' must consist from at least 4 path elements separated by '/', host aside: for example: https://github.com/1/2/3/4")
        }
        end
    }

    private LocalModuleConf newLocalModuleConf(String confLine) {
        /**
         * Examples of confLine:
         *
         * - ~/my-ydsu-module/src/main/groovy/ydsu/module/mymodule
         * - "${AppDataDirConf.CACHE}yesday/ydsu-module/src/main/groovy/ydsu/module/publicmodule":
         *   this is actually a system path for a public module passed as confLine
         */
        File homeDir = new File(confLine.startsWith('~') ?
                confLine.replace('~', AppDataDirConf.USER_HOME) : confLine)
        List<String> values = extractFields(confLine)
        new LocalModuleConf(confLine, values[0], values[1], values[2], homeDir)
    }

    /**
     * Extract the fields owner, repository and relativePath from the given {@code confLine}. The repository name will
     * be the parent of src or the first directory after user home.
     *
     * Examples:
     *
     * - ~/path/to/owner/mymodule/src will return owner, mymodule, src
     * - ~/path/to/owner/mymodule will return user.name, path, to/owner/mymodule
     *
     * @param confLine The configuration line like ~/my-ydsu-module/src/main/groovy/ydsu/module/mymodule
     * @return the fields owner, repository and relativePath
     */
    @PackageScope
    List<String> extractFields(String confLine) {
        List<String> tokens = [System.getProperty('user.name')]
        if (confLine.startsWith('~/')) {
            tokens.addAll(confLine.substring(2).split('/'))
        } else if (confLine.startsWith(AppDataDirConf.USER_HOME)) {
            tokens.addAll(confLine.substring(AppDataDirConf.USER_HOME.size() + 1).split('/'))
        } else {
            tokens.addAll(confLine.substring(1).split('/'))
        }
        if (tokens.size() < 3) {
            throw new IllegalArgumentException("Argument 'confLine' must consist from at least 2 path elements separated by '/', user home aside")
        }
        String owner = tokens[0]
        String repository = tokens[1]
        String relativePath = tokens[2..tokens.size() - 1].join('/')
        /**
         * The repository will be the parent of src or the first directory after user home or root, whichever comes
         * first.
         */
        for (int i = 2; i < tokens.size(); i++) {
            if (tokens[i] == 'src' || tokens[i] == 'build') {
                owner = tokens[i - 2]
                repository = tokens[i - 1]
                relativePath = tokens[i..tokens.size() - 1].join('/')
                break
            }
        }
        [owner, repository, relativePath]
    }
}
