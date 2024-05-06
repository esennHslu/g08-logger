package ch.hslu.vsk.logger.common.dataobject;

import ch.hslu.vsk.logger.api.LogLevel;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

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
public final class LogMessageDo implements Serializable, Comparable<LogMessageDo> {
    @Serial
    private static final long serialVersionUID = 824312352809765853L;
    private final String source;
    private final String message;
    private final Instant createdAt;
    private final Instant processedAt;
    private final LogLevel level;

    private LogMessageDo(final Builder builder) {
        this.source = builder.source;
        this.message = builder.message;
        this.createdAt = builder.timestamp;
        this.processedAt = builder.processed;
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

    public Instant getProcessedAt() {
        return processedAt;
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
                && Objects.equals(this.processedAt, other.processedAt)
                && Objects.equals(this.level, other.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.source,
                this.message,
                this.createdAt,
                this.processedAt,
                this.level);
    }

    /**
     * Compares this to given {@code other} based on their creation timestamps, enforcing an ascending order.
     *
     * @param other the object to be compared.
     * @return {@code < 0} if this is before, {@code > 0} if this is after, else {@code 0}
     */
    @Override
    public int compareTo(final LogMessageDo other) {
        if (this.createdAt.isAfter(other.createdAt)) {
            return 1;
        }

        if (this.createdAt.isBefore(other.createdAt)) {
            return -1;
        }

        return 0;
    }

    /**
     * Builder for creating {@link LogMessageDo} instances.
     */
    public static final class Builder {
        private final String message;
        private String source;
        private Instant timestamp;
        private Instant processed;
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
         * Registers the given timestamp for the instant the log was processed. Should only be set on the server.
         *
         * @param timestamp Point of time when the log message was processed on the server
         * @return self for further configurations
         */
        @SuppressWarnings("checkstyle:hiddenField")
        public Builder processed(final Instant timestamp) {
            this.processed = timestamp;
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
