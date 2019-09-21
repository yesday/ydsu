package ydsu.module.util.source

import groovy.util.logging.Slf4j

/**
 * Utility methods to invoke OS commands from within Groovy code.
 */
@Slf4j
class Exec {
    /**
     * Execute the given {@code command} from the given (optional) {@code workDir}, and return it's output.
     *
     * Unlike the {@link #shell} method, this method handles a nonzero exit code when the underlying shell command
     * terminates with error. The exit code and relevant information are subsequently wrapped into a {@link
     * RuntimeException}. Thus, this method is a convenience method suitable for the majority of cases.
     *
     * @param command the command to execute
     * @param workDir optional work directory to execute the command from
     * @return the output of the command
     * @throws RuntimeException if the command exited with error
     */
    static String command(String command, File workDir = null) {
        StringBuilder output = new StringBuilder()
        int exitValue = shell(command, workDir, output, true)
        if (exitValue > 0) {
            throw new RuntimeException("$output: The following command exited with error value $exitValue: $command")
        }
        return output.toString()
    }

    /**
     * Execute a long command. Long commands are the ones that require their progress to be printed to command line
     * prior to their completion.
     *
     * Examples of a long command are:
     * <pre>
     *  docker build --pull -t myimage .
     *  git pull
     * </pre>
     *
     * @param command the command to execute
     * @param workDir optional work directory to execute the command from
     * @throws RuntimeException if the command exited with error
     */
    static void longCommand(String command, File workDir = null) {
        int exitValue = shell(command, workDir, null, true)
        if (exitValue > 0) {
            throw new RuntimeException("The following command exited with error value $exitValue: $command")
        }
    }

    /**
     * Execute the given {@code command} in shell and return the termination exit code. A nonzero exit code means the
     * command terminated with error.
     *
     * @param command the command to execute
     * @param workDir optional work directory to execute the command from
     * @param output optional {@link StringBuilder} to write the output to or {@code null} to write to log
     * @param echoCommandUponError {@code true} results in the command itself to be logged at error level upon
     *                             unsuccessful execution
     * @return termination exit code (0=success, >0=error)
     */
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