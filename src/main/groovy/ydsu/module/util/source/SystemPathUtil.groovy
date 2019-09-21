package ydsu.module.util.source

class SystemPathUtil {
    static File getScriptFile() {
        new File(SystemPathUtil.class.protectionDomain.codeSource.location.path)
    }
}
