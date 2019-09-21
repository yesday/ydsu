package ydsu.module.manage.conf

import ydsu.module.util.conf.AppDataDirConf
import ydsu.module.util.source.*

interface PathConf {
    ReadAppData READ_APP_DATA = AppDataFactory.newReadAppData()
    WriteAppData WRITE_APP_DATA = AppDataFactory.newWriteAppData()
    File MODULE_READ_CONF = READ_APP_DATA.conf('module.yml')
    File MODULE_WRITE_CONF = WRITE_APP_DATA.conf('module.yml')
    String YDSU_SOURCE = "${AppDataDirConf.SOURCE}yesday/ydsu/"
    String CLASS_PATH_ROOT = "${YDSU_SOURCE}src/main/groovy/"
    String MODULES_PARENT_DIR = "${CLASS_PATH_ROOT}ydsu/module/"
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
    File SYSTEM_PATH_WRITE_PLAYBOOK = WRITE_APP_DATA.cache('system-path-playbook.yml')
    File GROOVY_COMPILER_WRITE_CONF = WRITE_APP_DATA.conf('groovyCompilerConf.groovy')
    String PUBLIC_MODULES_SOURCE_DIR = PublicModuleConf.MODULE_SOURCE
}