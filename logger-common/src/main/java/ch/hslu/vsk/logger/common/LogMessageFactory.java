package ch.hslu.vsk.logger.common;

import java.io.Serial;

public class LogMessageFactory {

    /**
     * @param message the log message being communicated
     * @return object of LogMessageImpl
     */
    public static LogMessage createLogMessage(final String message) {
        return new LogMessageImpl(message);
    }

    /**
     * An implementation of the {@link LogMessage} interface representing a log message with a string message.
     */
    private static final class LogMessageImpl implements LogMessage {
        /**
         * Default serial version UID for serialization.
         */
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * The message associated with this log entry.
         */
        private final String message;

        /**
         * Constructs a new LogMessageImpl object with the specified message.
         *
         * @param message the message to be associated with this log entry
         */
        private LogMessageImpl(final String message) {
            this.message = message;
        }

        /**
         * Retrieves the message associated with this log entry.
         *
         * @return the message associated with this log entry
         */
        @Override
        public String getMessage() {
            return message;
        }
    }

}
