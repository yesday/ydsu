package ydsu.module.util.source

enum OSFamily {
    LINUX,
    CYGWIN,
    DARWIN,
    SOLARIS,
    FREEBSD

    static OSFamily getInstance() {
        String uname = 'uname'.execute().text
        switch (uname) {
            case ~/Linux[\s\S]*/: return LINUX
            case ~/CYGWIN[\s\S]*/: return CYGWIN
            case ~/Darwin[\s\S]*/: return DARWIN
            case ~/SunOS[\s\S]*/: return SOLARIS
            case ~/FreeBSD[\s\S]*/: return FREEBSD
            default: throw new UnsupportedOperationException("OS family $uname is not recognised")
        }
    }
}