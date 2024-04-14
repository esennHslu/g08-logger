package ch.hslu.vsk.logger.component;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.api.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * The {@code RemoteLogger} class provides functionality for sending log messages to a remote log server.
 * It uses an implementation of the {@code LoggerSetup} class to do so.
 */
public class RemoteLogger implements Logger {
    private final LoggerClient loggerClient;

    /**
     * Logger implementation to log to remote system.
     *
     * @param loggerClient instance of logger client
     */
    RemoteLogger(final LoggerClient loggerClient) {
        this.loggerClient = loggerClient;
    }

    /**
     * Logs a debug message. This level of logging should be used for detailed information
     * that is typically of use only when diagnosing problems.
     *
     * @param s the message string to log
     */
    @Override
    public void debug(final String s) {
        this.loggerClient.sendLog(s, LogLevel.Debug);
    }

    /**
     * Logs an informational message. This level of logging should be used for messages that
     * highlight the progress of the application at a coarse-grained level.
     *
     * @param s the message string to log
     */
    @Override
    public void info(final String s) {
        this.loggerClient.sendLog(s, LogLevel.Info);
    }

    /**
     * Logs a warning message. This level of logging should be used for potentially harmful situations.
     *
     * @param s the message string to log
     */
    @Override
    public void warn(final String s) {
        this.loggerClient.sendLog(s, LogLevel.Warning);
    }

    /**
     * Logs an error message. This level of logging should be used for error events that
     * might still allow the application to continue running.
     *
     * @param s the message string to log
     */
    @Override
    public void error(final String s) {
        this.loggerClient.sendLog(s, LogLevel.Error);
    }

    /**
     * Logs an error message along with the stack trace of an exception. This level of logging
     * should be used for error events that are of considerable importance and will prevent normal
     * program execution. The stack trace of the exception is appended to the message.
     *
     * @param s the message string to log
     * @param e the exception to log, including its stack trace
     */
    @Override
    public void error(final String s, final Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        this.loggerClient.sendLog(s + sw.toString(), LogLevel.Error);
    }

    /**
     * Logs a message with a specific logging level. This generic logging method can be used
     * to log messages with any log level specified by the caller.
     *
     * @param logLevel the level of the log message
     * @param s the message string to log
     */
    @Override
    public void log(final LogLevel logLevel, final String s) {
        this.loggerClient.sendLog(s, logLevel);
    }
}
