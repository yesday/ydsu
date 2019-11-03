package ydsu.module.manage.conf

import ydsu.module.util.source.AppDataFactory
import ydsu.module.util.source.OSFamily
import ydsu.module.util.source.WriteAppData

interface PathConf {
    /**
     * - .bash_profile is executed for login shells
     * - .bashrc is executed for interactive non-login shells
     * - Unlike other UNIX systems, on macOS, Terminal by default runs a login shell every time, meaning that
     *   .bash_profile is executed every time
     */
    String BASH_CONF = OSFamily.getInstance() == OSFamily.DARWIN ? '.bash_profile' : '.bashrc'
    String EXTEND_BASH_CONF = ".extend$BASH_CONF"
    String ZSH_CONF = '.zshrc'
    String EXTEND_ZSH_CONF = ".extend$ZSH_CONF"
    String SYSTEM_PATH_CONF_TITLE = 'ydsu ansible managed block for system path'
    String SYSTEM_PATH_CONF_MARKER = "# {mark} ${SYSTEM_PATH_CONF_TITLE}"
    WriteAppData WRITE_APP_DATA = AppDataFactory.newWriteAppData()
    File SYSTEM_PATH_WRITE_PLAYBOOK = WRITE_APP_DATA.cache('system-path-playbook.yml')
    File GROOVY_COMPILER_WRITE_CONF = WRITE_APP_DATA.conf('groovyCompilerConf.groovy')
}