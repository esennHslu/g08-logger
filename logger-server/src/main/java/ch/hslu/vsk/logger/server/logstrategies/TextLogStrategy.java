package ch.hslu.vsk.logger.server.logstrategies;

import ch.hslu.vsk.logger.common.dataobject.LogMessageDo;
import ch.hslu.vsk.logger.server.LogStrategy;

import java.text.MessageFormat;

/**
 * Implements a logging strategy to format log messages as plain text.
 */
public final class TextLogStrategy implements LogStrategy {

    /**
     * Formats a log message into a plain text format.
     *
     * @param message the log message to format
     * @return a string in a readable text format
     */
    @Override
    public String format(final LogMessageDo message) {
        return MessageFormat.format("[{0}] [{1}] {2}: {3}",
                message.getCreatedAt(),
                message.getLevel(),
                message.getSource(),
                message.getMessage());
    }
}
