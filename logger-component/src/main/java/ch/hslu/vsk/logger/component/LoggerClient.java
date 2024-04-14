package ch.hslu.vsk.logger.component;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.api.Logger;
import ch.hslu.vsk.logger.api.LoggerSetup;
import ch.hslu.vsk.logger.api.LoggerSetupBuilder;
import ch.hslu.vsk.logger.common.dataobject.LogMessageDo;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URI;
import java.nio.file.Path;
import java.time.Instant;

/**
 * The {@code LoggerClient} class provides functionalities to log messages to a remote server.
 * It supports setting a minimum log level and specifying a target server for logging.
 * <br />
 * Usage example:
 * <pre>
 *  LoggerSetup loggerClient = new LoggerClient.Builder()
 *          .requires(LogLevel.Info)
 *          .from("client")
 *          .usesAsFallback(Path.of("/dev", "null"))
 *          .targetsServer(URI.create("http://localhost:9999"))
 *          .build();
 *  client.info("This is an info-level message");
 * </pre>
 */
public class LoggerClient implements LoggerSetup {
    private ObjectOutputStream outputStream;
    private LogLevel minLogLevel;
    private final String source;

    protected LoggerClient(final Builder builder) {

        this.minLogLevel = builder.minLogLevel;
        this.source = builder.source;
        Path fallbackFile = builder.fallbackFile; //TODO: Use this when implementing fallback
        URI targetServerAddress = builder.targetServerAddress;

        try {
            @SuppressWarnings("resource")
            Socket socket = new Socket(targetServerAddress.getHost(), targetServerAddress.getPort());
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException ioException) {
            // TODO: Handle case connection not established
            System.out.println(ioException.getMessage());
        }
    }

    /**
     * Creates an Instance of the RemoteLogger.
     * @return Instance of RemoteLogger
     */
    @Override
    public Logger createLogger() {
        return new RemoteLogger(this);
    }

    /**
     * Retrieves the current log level setting.
     *
     * @return the current minimum log level
     */
    @Override
    public LogLevel getMinLogLevel() {
        return this.minLogLevel;
    }

    /**
     * Set the minimum log level for the logger client.
     *
     * @param logLevel The loglevel to be set as minimum
     */
    @Override
    public void setMinLogLevel(final LogLevel logLevel) {
        this.minLogLevel = logLevel;
    }

    /**
     * Sends a log to the remote target.
     *
     * @param message The message to send.
     * @param level The LogLevel of the message.
     */
    public void sendLog(final String message, final LogLevel level) {
        if (level.compareTo(this.minLogLevel) > 0) {
            return; // do not log if log level is below minimum level
        }

        Instant timestamp = Instant.now();

        LogMessageDo messageDo = new LogMessageDo.Builder(message)
                .from(source)
                .at(timestamp)
                .level(level)
                .build();

        try {
            outputStream.writeObject(messageDo);
            outputStream.flush();
        } catch (IOException ioException) {
            // TODO: Log to fallback
            System.out.println(ioException.getMessage());
        }
    }

    /**
     * Builder for {@code LoggerClient}. It allows for configuring the {@code LoggerClient} instance
     * with a fluent interface.
     * <br />
     * Example usage:
     * <pre>
     * LoggerClient client = new LoggerClient.Builder()
     *                          .minLogLevel(LogLevel.DEBUG)
     *                          .targetServer("http://example.com")
     *                          .build();
     * </pre>
     */
    public static class Builder implements LoggerSetupBuilder {

        private LogLevel minLogLevel;
        private String source;
        private Path fallbackFile;
        private URI targetServerAddress;

        /**
         * Sets the minimum log level for the logger client.
         * Messages with a lower log level will not be sent to the target server.
         *
         * @param logLevel the minimum {@code LogLevel} to be logged
         * @return the builder instance for chaining
         */
        @Override
        public LoggerSetupBuilder requires(final LogLevel logLevel) {
            this.minLogLevel = logLevel;
            return this;
        }

        /**
         * Sets the source that logs.
         *
         * @param s Name of the client creating logs
         * @return the builder instance for chaining
         */
        @Override
        public LoggerSetupBuilder from(final String s) {
            this.source = s;
            return this;
        }

        /**
         * Sets the fallback path on the LoggerClient.
         * @param path Path of the fallback file.
         * @return the builder instance for chaining
         */
        @Override
        public LoggerSetupBuilder usesAsFallback(final Path path) {
            this.fallbackFile = path;
            return this;
        }

        /**
         * Sets the target server URL where log messages will be sent.
         *
         * @param uri the address to the logging server.
         * @return the builder instance for chaining
         */
        @Override
        public LoggerSetupBuilder targetsServer(final URI uri) {
            this.targetServerAddress = uri;
            return this;
        }

        /**
         * Constructs the {@code LoggerClient} with the configured settings.
         *
         * @return the configured {@code LoggerClient} instance
         */
        @Override
        public LoggerSetup build() {
            return new LoggerClient(this);
        }
    }
}
