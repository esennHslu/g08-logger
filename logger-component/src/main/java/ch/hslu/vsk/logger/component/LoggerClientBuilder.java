package ch.hslu.vsk.logger.component;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.api.LoggerSetup;
import ch.hslu.vsk.logger.api.LoggerSetupBuilder;

import java.net.URI;
import java.nio.file.Path;

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
public final class LoggerClientBuilder implements LoggerSetupBuilder {

    private LogLevel minLogLevel;
    private String source;
    private Path fallbackFile;
    private URI targetServerAddress;

    public LogLevel getMinLogLevel() {
        return minLogLevel;
    }

    public String getSource() {
        return source;
    }

    public Path getFallbackFile() {
        return fallbackFile;
    }

    public URI getTargetServerAddress() {
        return  targetServerAddress;
    }

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