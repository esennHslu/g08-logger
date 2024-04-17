package ch.hslu.vsk.logger.common.dataobject;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import ch.hslu.vsk.logger.api.LogLevel;

/**
 * Immutable POJO holding all fields of a log message during transmission from logger-component to logger-server.
 * <br />
 * Sample Usage:
 * <pre>
 * LogMessageDo log = new LogMessageDo.Builder("this is a message")
 *                 .from("test-system")
 *                 .at(Instant.now())
 *                 .level(LogLevel.INFO)
 *                 .build();
 * </pre>
 */
public final class LogMessageDo implements Serializable {
    @Serial
    private static final long serialVersionUID = 8658874023243910484L;
    private final String source;
    private final String message;
    private final Instant createdAt;
    private final LogLevel level;

    private LogMessageDo(final Builder builder) {
        this.source = builder.source;
        this.message = builder.message;
        this.createdAt = builder.timestamp;
        this.level = builder.level;
    }

    public String getSource() {
        return source;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public LogLevel getLevel() {
        return level;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof final LogMessageDo other)) {
            return false;
        }

        return Objects.equals(this.source, other.source)
                && Objects.equals(this.message, other.message)
                && Objects.equals(this.createdAt, other.createdAt)
                && Objects.equals(this.level, other.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.source, this.message, this.createdAt, this.level);
    }

    /**
     * Builder for creating {@link LogMessageDo} instances.
     */
    public static final class Builder {
        private final String message;
        private String source;
        private Instant timestamp;
        private LogLevel level;

        /**
         * Creates a new builder instance while registering the message of the log to be created.
         *
         * @param message Message which should be logged
         */
        public Builder(final String message) {
            this.message = message;
        }

        /**
         * Registers the given source for the log to be created.
         *
         * @param source Name of the source system
         * @return self for further configuration
         */
        @SuppressWarnings("checkstyle:hiddenField")
        public Builder from(final String source) {
            this.source = source;
            return this;
        }

        /**
         * Registers the given timestamp for the log to be created.
         *
         * @param timestamp Point of time when the log message was created by the client
         * @return self for further configurations
         */
        @SuppressWarnings("checkstyle:hiddenField")
        public Builder at(final Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /**
         * Registers the given level for the log to be created.
         *
         * @param level Log level (severity, importance) of the log message
         * @return self for further configurations
         */
        @SuppressWarnings("checkstyle:hiddenField")
        public Builder level(final LogLevel level) {
            this.level = level;
            return this;
        }

        /**
         * Creates a {@link LogMessageDo} with the previously applied configurations.
         * Doesn't perform any validation in regard to presence and validity of configured attributes on purpose
         * - this is the responsibility of the caller.
         *
         * @return Constructed {@link LogMessageDo} instance
         */
        public LogMessageDo build() {
            return new LogMessageDo(this);
        }
    }
}
