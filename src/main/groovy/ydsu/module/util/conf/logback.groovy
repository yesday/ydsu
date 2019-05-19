package ydsu.module.util.conf

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.ThresholdFilter

import static ch.qos.logback.classic.Level.*

/*
 * Log Levels
 *
 * - priority: ALL < TRACE < DEBUG < INFO < WARN < ERROR < OFF or INHERITED, or its synonym NULL
 * - The special case-insensitive value INHERITED, or its synonym NULL, will force the level of
 *   the logger to be inherited from higher up in the hierarchy.
 *
 * See Also
 *
 * - http://stackoverflow.com/questions/7839565/logging-levels-logback-rule-of-thumb-to-assign-log-levels
 */
// statusListener(OnConsoleStatusListener)
appender("STDOUT", ConsoleAppender) {
    filter(ThresholdFilter) {
        level = INFO
    }
    encoder(PatternLayoutEncoder) {
        /**
         * The part {@code %replace(%-6level){'INFO  ', ''}} removes the 'INFO' prefix from the resulted log messages.
         * This is useful when a command line application uses the {@link ch.qos.logback.core.ConsoleAppender} to print
         * application messages. In the latter case a typical application would NOT want to print regular {@link
         * ch.qos.logback.classic.Level # INFO} messages prefixed with the logging severity level, however when a
         * warning or error occurs then the message should be prefixed with the severity level. For example, "Welcome to
         * my application" is preferred to "INFO: Welcome to my application". Conversely, "ERROR: please provide valid
         * parameter" is preferred to "please provide valid parameter".
         * <p>
         * Some other example patterns to experiment with are
         * <ul>
         * <li>{@code pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"}</li>
         * <li>{@code pattern = "%-5level %msg%n"}</li>
         * </ul>
         */
        pattern = "%replace(%-6level){'INFO  ', ''}%msg%n"
    }
}
/**
 * In accordance with the https://12factor.net/logs[Logs] best practice from the
 * https://12factor.net[Twelve-Factor App Practices], logs are written as an ordered event stream to stdout so that the
 * logfiles will be managed by the execution environment.
 */
root(ALL, ["STDOUT"])
logger("org.springframework", WARN)
logger("org.hibernate", WARN)
// Equivalent to hibernate.show_sql=true
//logger("org.hibernate.SQL", DEBUG)
// Prints the bound parameters among other things
//logger("org.hibernate.type", TRACE)
