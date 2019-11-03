package ydsu.module.util.source

import ydsu.module.util.conf.AppDataDirConf

class Args {
    static def constraint(String argName, Object argValue, List<ArgsConstraint> constraints,
                          Map<ArgsConstraint, List<Object>> constraintParams = null) {
        def tidied = argValue
        if (constraints && constraints.size()) {
            for (ArgsConstraint constraint : constraints) {
                switch (constraint) {
                    case ArgsConstraint.NOT_NULL:
                        notNull(argName, tidied)
                        break
                    case ArgsConstraint.NOT_BLANK:
                        notBlank(argName, tidied as String)
                        break
                    case ArgsConstraint.MIN_SIZE:
                        minSize(argName, tidied, constraintParams[constraint][0])
                        break
                    case ArgsConstraint.MAX_SIZE:
                        maxSize(argName, tidied, constraintParams[constraint][0])
                        break
                    case ArgsConstraint.SIZE:
                        size(argName, tidied, constraintParams[constraint][0], constraintParams[constraint][1])
                        break
                    case ArgsConstraint.EXISTS:
                        exists(argName, tidied as File)
                        break
                    case ArgsConstraint.DIRECTORY:
                        directory(argName, tidied as File)
                        break
                    case ArgsConstraint.FILE:
                        file(argName, tidied as File)
                        break
                    case ArgsConstraint.SYSTEM_PATH:
                        tidied = systemPath(argName, tidied)
                        break
                    case ArgsConstraint.CONF_LINE:
                        tidied = confLine(argName, tidied as String)
                        break
                    default:
                        throw new IllegalArgumentException("Unknown constraint type: $constraint")
                }
            }
        }
        tidied
    }

    static notNull(String argName, Object argValue) {
        if (argValue == null) {
            throw new IllegalArgumentException("Argument '$argName' must not be null")
        }
    }

    static notBlank(String argName, String argValue) {
        if (argValue == null || argValue.isEmpty() || argValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Argument '$argName' must not be blank")
        }
    }

    static minSize(String argName, String argValue, int min) {
        if (argValue) {
            if (argValue.size() < min) {
                throw new IllegalArgumentException("$argName must be at least $min characters long: $argValue")
            }
        }
    }

    static maxSize(String argName, String argValue, int max) {
        if (argValue) {
            if (argValue.size() > max) {
                throw new IllegalArgumentException("$argName must be $max characters long maximum: $argValue")
            }
        }
    }

    static size(String argName, String argValue, int min, int max) {
        minSize(argName, argValue, min)
        maxSize(argName, argValue, max)
    }

    static exists(String argName, File argValue) {
        notNull(argName, argValue)
        if (!argValue.exists()) {
            throw new IllegalArgumentException("File '$argName' does not exist: $argValue")
        }
    }

    static directory(String argName, File argValue) {
        notNull(argName, argValue)
        if (!argValue.isDirectory()) {
            throw new IllegalArgumentException("Argument '$argName' is not a directory: $argValue")
        }
    }

    static file(String argName, File argValue) {
        notNull(argName, argValue)
        if (!argValue.isFile()) {
            throw new IllegalArgumentException("Argument '$argName' is not a file: $argValue")
        }
    }

    static File systemPath(String argName, File argValue) {
        notNull(argName, argValue)
        exists(argName, argValue)
        directory(argName, argValue)
        if (argValue.name.toLowerCase() == '.git') {
            throw new IllegalArgumentException("$argName directory cannot be named .git: $argValue")
        }
        if (argValue.toString().toLowerCase().contains('/.git/')) {
            throw new IllegalArgumentException("$argName cannot reside inside a .git directory: $argValue")
        }
        argValue
    }

    static String systemPath(String argName, String argValue) {
        notBlank(argName, argValue)
        String tidied = argValue.trim()
        tidied = tidied.startsWith('~') ? tidied.replace('~', AppDataDirConf.USER_HOME) : tidied
        systemPath(argName, new File(tidied))
        tidied
    }

    static String confLine(String argName, String argValue) {
        notBlank(argName, argValue)
        String tidied = argValue.trim()
        minSize(argName, tidied, 3)
        tidied = tidied.startsWith('~') ? tidied.replace('~', AppDataDirConf.USER_HOME) : tidied
        if (tidied == AppDataDirConf.USER_HOME) {
            throw new IllegalArgumentException("Argument '$argName' cannot be equal to the user home directory: ${AppDataDirConf.USER_HOME}")
        }
        tidied
    }
}
