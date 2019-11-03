package ydsu.module.util.conf
/**
 * For documentation refer to
 *
 * - http://docs.groovy-lang.org/latest/html/api/org/codehaus/groovy/control/CompilerConfiguration.html
 * - https://mrhaki.blogspot.gr/2016/01/groovy-goodness-customising-groovy.html
 *
 * Starting from the src/main/groovy directory, add all the subdirectories recursively to the classpath
 * (src/main/groovy/ydsu/module/util/../../../).
 */
List<String> dirs = []
File groovyDir = new File(getClass().protectionDomain.codeSource.location.path).parentFile.parentFile.parentFile.parentFile.parentFile
dirs << groovyDir.toString()
groovyDir.eachDirRecurse {
    dirs << it.toString()
}
String ydsuClasspath = System.getenv('YDSU_CLASSPATH')
if (ydsuClasspath) {
    dirs.addAll(ydsuClasspath.tokenize(':'))
}
configuration.setClasspathList(dirs)
// configuration.setScriptBaseClass('common.basic.source.LoggingBaseScript')
