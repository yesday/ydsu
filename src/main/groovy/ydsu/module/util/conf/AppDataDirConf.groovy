package ydsu.module.util.conf

interface AppDataDirConf {
    String USER_HOME = System.getProperty('user.home')
    String APP_HOME = "${USER_HOME}/appdata/ydsu/"

    //region Types of Application Data
    /**
     * Source code.
     * Examples: shell scripts, apache public_html, php files, asciidoc files, ansible playbooks, Dockerfile,
     *           any application source code
     * Updated by: sysadmin
     */
    String SOURCE = "${APP_HOME}source/"
    /**
     * Build files.
     * Examples: webapps/myapp.war, maven .~/m2, jar files, binary files
     * Updated by: sysadmin
     */
    String BUILD = "${APP_HOME}build/"
    /**
     * Configuration.
     * Examples: httpd.conf, mariadb.conf, mounted docker volumes for configuration, application configuration
     * Updated by: sysadmin
     */
    String CONF = "${APP_HOME}conf/"
    /**
     * Sensitive data / secrets.
     * Examples: docker secrets, non-versioned application secrets, passwords, certificates, secret configuration
     * Updated by: sysadmin
     */
    String SECRET = "${APP_HOME}secret/"
    /**
     * Log files.
     * Examples: logs produced by the scripts (refer to logback.groovy)
     * Updated by: app
     */
    String LOG = "${APP_HOME}log/"
    /**
     * Application data. Stores application state.
     * Examples: database data tables, git repositories, file uploads
     * Updated by: app
     */
    String DATA = "${APP_HOME}data/"
    /**
     * Cache. Stores cache generated by the app. As opposite to the {@link #DATA} directory it should be always safe
     * deleting the cache.
     * Examples: sessions, html files generated by asciidoctor, ehcache, chrome cache, downloaded packages of a package
     *           manager, temp data
     * Updated by: app
     */
    String CACHE = "${APP_HOME}cache/"
    /**
     * Generated documents.
     * Examples: html files converted from asciidoc by asciidoctor, javadoc
     * Updated by: app
     */
    String DOC = "${APP_HOME}doc/"
    /**
     * Generated reports (non-source reports).
     * Examples: PDF reports produced by JasperReports
     * Updated by: app
     */
    String REPORT = "${APP_HOME}report/"
    //endregion
}