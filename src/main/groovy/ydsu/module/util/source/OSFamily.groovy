package ydsu.module.util.source

enum OSFamily {
    UNKNOWN,
    LINUX,
    CYGWIN,
    DARWIN,
    SOLARIS,
    FREEBSD;

    @Lazy
    private static volatile OSFamily lazySingleton = { newInstance() }()

    static OSFamily getInstance() {
        lazySingleton
    }

    static OSFamily newInstance() {
        String uname
        try {
            uname = 'uname'.execute().text
        } catch (ignored) {
            return UNKNOWN
        }
        switch (uname) {
            case ~/Linux[\s\S]*/: return LINUX
            case ~/CYGWIN[\s\S]*/: return CYGWIN
            case ~/Darwin[\s\S]*/: return DARWIN
            case ~/SunOS[\s\S]*/: return SOLARIS
            case ~/FreeBSD[\s\S]*/: return FREEBSD
            default: return UNKNOWN
        }
        UNKNOWN
    }
}