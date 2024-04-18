package ch.hslu.vsk.logger.server;

import ch.hslu.vsk.logger.common.dataobject.LogMessageDo;

/**
 * Defines a strategy for formatting log messages.
 */
public interface LogStrategy {
    /**
     * Formats a given log message.
     *
     * @param message the log message to format
     * @return a formatted string representation of the log message
     */
    String format(LogMessageDo message);
}
