package ch.hslu.vsk.logger.server.logstrategies;

import ch.hslu.vsk.logger.common.dataobject.LogMessageDo;
import ch.hslu.vsk.logger.server.LogStrategy;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Implements a logging strategy to format log messages for the competition.
 */
public class CompetitionStrategy implements LogStrategy {

    /**
     * Formats a log message into the format for the competition.
     *
     * @param message the log message to format
     * @return the formatted log message
     */
    @Override
    public String format(final LogMessageDo message) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSS")
                .withZone(ZoneId.of("UTC"));

        String timestamp = formatter.format(message.getCreatedAt());

        return String.format("%s %s %s %s",
                timestamp,
                (message.getLevel()).toString().toUpperCase(Locale.getDefault()),
                message.getSource(),
                message.getMessage());
    }
}
