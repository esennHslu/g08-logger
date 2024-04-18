package ch.hslu.vsk.logger.server.logstrategies;

import ch.hslu.vsk.logger.common.dataobject.LogMessageDo;
import ch.hslu.vsk.logger.server.LogStrategy;

import java.text.MessageFormat;

/**
 * Implements a logging strategy to format log messages as CSV.
 */
public final class CSVLogStrategy implements LogStrategy {

    /**
     * Formats a log message into a CSV line.
     *
     * @param message the log message to format
     * @return a CSV string containing the log message attributes
     */
    @Override
    public String format(final LogMessageDo message) {
        return MessageFormat.format("{0}, {1}, {2}, {3}",
                escapeCsv(message.getCreatedAt().toString()),
                escapeCsv(message.getLevel().toString()),
                escapeCsv(message.getSource()),
                escapeCsv(message.getMessage()));
    }

    /**
     * Escapes CSV characters in a string input.
     *
     * @param input the string to be escaped
     * @return an escaped CSV string
     */
    private String escapeCsv(final String input) {
        String escaped = input;
        // Check if escaping is needed (if input contains comma, double-quote, or newline)
        if (input.contains(",") || input.contains("\"") || input.contains("\n")) {
            // Escape double-quotes by doubling them
            escaped = input.replace("\"", "\"\"");
            // Wrap the escaped input in double-quotes
            escaped = "\"" + escaped + "\"";
        }
        return escaped;
    }
}
