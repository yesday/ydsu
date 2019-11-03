package ydsu.module.util.source

import groovy.transform.PackageScope
import ydsu.module.util.conf.AppDataDirConf

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

import static ydsu.module.util.source.Args.notBlank

@Grab(group = 'javax.validation', module = 'validation-api', version = '2.0.1.Final', transitive = false)
class ModuleAttributeParser {
    /**
     * Argument name 'confLine' is used instead of 'baseDir' because that's how it's known from the public method {@link
     * #parse(String)}
     */
    private static final String MIN_TWO_ELEMENTS_MSG = "Argument 'confLine' must consist from at least 2 path elements separated by '/', user home aside"
    private String publicModulesSourceDir

    /**
     * Constructs an attribute parser for modules. If the optional {@code publicModulesSourceDir} is provided, it will
     * help determining the case where {@code confLine} is a system path of a public module. In the latter case, having
     * the {@code publicModulesSourceDir} will allow faster parsing of attributes and will set the {@link
     * ModuleAttribute#PUBLIC} to true.
     *
     * @param publicModulesSourceDir the source directory where public modules get cloned to
     */
    ModuleAttributeParser(@NotBlank String publicModulesSourceDir = AppDataDirConf.PUBLIC_MODULES_SOURCE) {
        notBlank 'publicModulesSourceDir', publicModulesSourceDir
        this.publicModulesSourceDir = publicModulesSourceDir.endsWith('/') ? publicModulesSourceDir : publicModulesSourceDir + '/'
    }

    @NotNull
    @NotEmpty
    EnumMap<ModuleAttribute, String> parse(@NotBlank String confLine) {
        String tidied = Args.confLine('confLine', confLine)
        if (tidied.startsWith('https://')) {
            return parseHttpsUrl(tidied)
        }
        if (tidied.startsWith(AppDataDirConf.INTERNAL_MODULES_REPOSITORY)) {
            return parseBaseDirOfInternalModuleGroup(tidied)
        }
        if (tidied.startsWith(publicModulesSourceDir)) {
            return parseBaseDirOfPublicModuleGroup(tidied)
        }
        if (tidied.charAt(0) == '/') {
            return parseBaseDirOfLocalModuleGroup(tidied)
        }
        String msg = """
Argument 'confLine' is not formatted correctly: $confLine: should start with https:// or / or ~: Examples:
- https://github.com/yesday/ydsu-module/master/src/main/groovy/ydsu/module/skeleton
- /full/path/to/another/local/modules-project/src/main/groovy/ydsu/module
- ~/some/local/modules-project/src/main/groovy/ydsu/module
"""
        throw new IllegalArgumentException(msg)
    }

    @PackageScope
    EnumMap<ModuleAttribute, String> parseHttpsUrl(String httpsUrl) {
        EnumMap<ModuleAttribute, String> attribute = new EnumMap<>(ModuleAttribute.class)
        attribute[ModuleAttribute.INTERNAL] = 'false'
        attribute[ModuleAttribute.PUBLIC] = 'true'
        int begin = 8 // https:// = 8 characters
        int end = endIndex(httpsUrl, begin)
        attribute[ModuleAttribute.HOST] = httpsUrl.substring(begin, end)
        begin = end + 1
        end = endIndex(httpsUrl, begin)
        attribute[ModuleAttribute.OWNER] = httpsUrl.substring(begin, end)
        begin = end + 1
        end = endIndex(httpsUrl, begin)
        attribute[ModuleAttribute.REPOSITORY] = httpsUrl.substring(begin, end)
        attribute[ModuleAttribute.HTTPS_CLONE_URL] = "https://${attribute[ModuleAttribute.HOST]}/${attribute[ModuleAttribute.OWNER]}/${attribute[ModuleAttribute.REPOSITORY]}.git".toString()
        begin = end + 1
        end = endIndex(httpsUrl, begin)
        attribute[ModuleAttribute.BRANCH] = httpsUrl.substring(begin, end)
        begin = end + 1
        attribute[ModuleAttribute.RELATIVE_PATH] = httpsUrl.substring(begin)
        attribute[ModuleAttribute.SOURCE_DIR] = publicModulesSourceDir
        attribute[ModuleAttribute.BASE_DIR] = "${publicModulesSourceDir}${attribute[ModuleAttribute.OWNER]}/${attribute[ModuleAttribute.REPOSITORY]}/${attribute[ModuleAttribute.RELATIVE_PATH]}".toString()
        attribute
    }

    private static endIndex(String confLine, int begin) {
        int end = confLine.indexOf('/', begin)
        if (end < 0) {
            throw new IllegalArgumentException("Argument 'confLine' must consist from at least 4 path elements separated by '/', host aside: for example: https://github.com/1/2/3/4")
        }
        end
    }

    @PackageScope
    EnumMap<ModuleAttribute, String> parseBaseDirOfInternalModuleGroup(String baseDir) {
        // Example: /home/user/appdata/ydsu/source/yesday/ydsu/src/main/groovy/ydsu/module
        EnumMap<ModuleAttribute, String> attribute = new EnumMap<>(ModuleAttribute.class)
        attribute[ModuleAttribute.INTERNAL] = 'true'
        attribute[ModuleAttribute.PUBLIC] = 'true'
        attribute[ModuleAttribute.OWNER] = 'yesday'
        attribute[ModuleAttribute.REPOSITORY] = 'ydsu'
        attribute[ModuleAttribute.HTTPS_CLONE_URL] = 'https://github.com/yesday/ydsu.git'
        attribute[ModuleAttribute.BRANCH] = 'master'
        attribute[ModuleAttribute.RELATIVE_PATH] = baseDir.substring(AppDataDirConf.INTERNAL_MODULES_REPOSITORY.size())
        attribute[ModuleAttribute.SOURCE_DIR] = AppDataDirConf.SOURCE
        attribute[ModuleAttribute.BASE_DIR] = baseDir
        attribute
    }

    /**
     * Parse the attribute OWNER, REPOSITORY and RELATIVE_PATH from the given {@code baseDir}, which is inside
     * {@link #publicModulesSourceDir}.
     *
     * The method {@link #parseBaseDirOfLocalModuleGroup(java.lang.String)} can be used instead, however, it has
     * slightly higher complexity and sets the PUBLIC attribute to false instead of true.
     *
     * @param baseDir The base directory, without the tilde (~) character, inside {@link #publicModulesSourceDir}
     * @return Enum Map with the attributes OWNER, REPOSITORY, RELATIVE_PATH and BASE_DIR
     */
    @PackageScope
    EnumMap<ModuleAttribute, String> parseBaseDirOfPublicModuleGroup(String baseDir) {
        // Example: /home/user/appdata/ydsu/cache/yesday-ydsu-module-publicmodulegroup/source/yesday/ydsu-module/src/main/groovy/ydsu/module/publicmodulegroup
        EnumMap<ModuleAttribute, String> attribute = new EnumMap<>(ModuleAttribute.class)
        attribute[ModuleAttribute.INTERNAL] = 'false'
        attribute[ModuleAttribute.PUBLIC] = 'true'
        List<String> tokens = baseDir.substring(publicModulesSourceDir.size()).split('/')
        if (tokens.size() < 3) {
            throw new IllegalArgumentException(MIN_TWO_ELEMENTS_MSG)
        }
        attribute[ModuleAttribute.OWNER] = tokens[0]
        attribute[ModuleAttribute.REPOSITORY] = tokens[1]
        attribute[ModuleAttribute.RELATIVE_PATH] = tokens[2..tokens.size() - 1].join('/')
        attribute[ModuleAttribute.SOURCE_DIR] = publicModulesSourceDir
        attribute[ModuleAttribute.BASE_DIR] = baseDir
        attribute
    }

    /**
     * Parse the attribute OWNER, REPOSITORY and RELATIVE_PATH from the given {@code baseDir}. The repository will
     * be the parent of src or the first directory after user home or root, whichever comes first.
     *
     * This method can be used in place of {@link #parseBaseDirOfPublicModuleGroup(java.lang.String)} but not vice
     * versa. The only difference is that the PUBLIC attribute will be false and the algorithm has slightly higher
     * complexity.
     *
     * Examples:
     *
     * - ~/path/to/owner/mymodule/src will return owner, mymodule, src
     * - ~/path/to/owner/mymodule will return user.name, path, to/owner/mymodule
     *
     * @param baseDir The base directory, without the tilde (~) character, like
     *                   /home/user/my-ydsu-module/src/main/groovy/ydsu/module/mymodule
     * @return Enum map with the attributes INTERNAL, PUBLIC, OWNER, REPOSITORY, RELATIVE_PATH and BASE_DIR
     */
    @PackageScope
    static EnumMap<ModuleAttribute, String> parseBaseDirOfLocalModuleGroup(String baseDir) {
        EnumMap<ModuleAttribute, String> attribute = new EnumMap<>(ModuleAttribute.class)
        attribute[ModuleAttribute.INTERNAL] = 'false'
        attribute[ModuleAttribute.PUBLIC] = 'false'
        List<String> tokens = [System.getProperty('user.name')]
        if (baseDir.startsWith(AppDataDirConf.USER_HOME)) {
            tokens.addAll(baseDir.substring(AppDataDirConf.USER_HOME.size() + 1).split('/'))
        } else {
            tokens.addAll(baseDir.substring(1).split('/'))
        }
        if (tokens.size() < 3) {
            throw new IllegalArgumentException(MIN_TWO_ELEMENTS_MSG)
        }
        attribute[ModuleAttribute.OWNER] = tokens[0]
        attribute[ModuleAttribute.REPOSITORY] = tokens[1]
        attribute[ModuleAttribute.RELATIVE_PATH] = tokens[2..tokens.size() - 1].join('/')
        /**
         * The repository will be the parent of src or the first directory after user home or root, whichever comes
         * first.
         */
        for (int i = 2; i < tokens.size(); i++) {
            if (tokens[i] == 'src' || tokens[i] == 'build') {
                attribute[ModuleAttribute.OWNER] = tokens[i - 2]
                attribute[ModuleAttribute.REPOSITORY] = tokens[i - 1]
                attribute[ModuleAttribute.RELATIVE_PATH] = tokens[i..tokens.size() - 1].join('/')
                break
            }
        }
        attribute[ModuleAttribute.BASE_DIR] = baseDir
        attribute
    }
}
