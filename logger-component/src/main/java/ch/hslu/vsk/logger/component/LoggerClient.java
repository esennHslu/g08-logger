package ch.hslu.vsk.logger.component;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.api.Logger;
import ch.hslu.vsk.logger.api.LoggerSetup;
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

    protected LoggerClient(final LoggerClientBuilder builder) {

        this.minLogLevel = builder.getMinLogLevel();
        this.source = builder.getSource();
        Path fallbackFile = builder.getFallbackFile(); //TODO: Use this when implementing fallback
        URI targetServerAddress = builder.getTargetServerAddress();

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
}
