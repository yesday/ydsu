package ydsu.module.util.lib

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Base script with logging dependencies.
 */
@GrabConfig(systemClassLoader = true)
@Grab(group = 'org.slf4j', module = 'slf4j-api', version = '1.7.26', transitive = false)
@Grab(group = 'ch.qos.logback', module = 'logback-core', version = '1.2.3', transitive = false)
@Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.2.3', transitive = false)
abstract class LoggingBaseScript extends Script {
    @Delegate
    final Logger log = LoggerFactory.getLogger(getClass())

    def run() {
        runCode()
    }

    /**
     * Abstract method as placeholder for the actual script code to run.
     * @return the result of the script execution
     */
    abstract def runCode()
}