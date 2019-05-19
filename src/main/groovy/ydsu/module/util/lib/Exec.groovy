package ydsu.module.util.lib

import groovy.util.logging.Slf4j

/**
 * Utility methods to invoke OS commands from within Groovy code.
 */
@Slf4j
class Exec {
    static String command(String command, File workDir = null) {
        StringBuilder output = new StringBuilder()
        int exitValue = shell(command, workDir, output, true)
        if (exitValue > 0) {
            throw new RuntimeException("$output: The following command exited with error value $exitValue: $command")
        }
        return output.toString()
    }

    static int shell(String command, File workDir = null, StringBuilder output = null,
                     boolean echoCommandUponError = false) {
        List<String> cmd = ['sh',
                            '-c',
                            command]
        ProcessBuilder pb = new ProcessBuilder(cmd)
                .redirectErrorStream(true)
        if (workDir) {
            pb.directory(workDir)
        }
        Map<String, String> env = pb.environment()
        env["PATH"] = "${env["PATH"]}:/usr/local/bin".toString()
        Process process = pb.start()
        if (output == null) {
            process.inputStream.eachLine {
                log.info it
            }
        } else {
            process.inputStream.eachLine {
                output.append(it).append('\n')
            }
            if (output.length()) {
                output.delete(output.length() - 1, output.length())
            }
        }
        process.waitFor()
        if (process.exitValue() && echoCommandUponError) {
            log.error command
        }
        return process.exitValue()
    }
}