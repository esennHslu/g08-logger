package ch.hslu.vsk.logger.component;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.api.Logger;
import ch.hslu.vsk.logger.api.LoggerSetup;
import ch.hslu.vsk.logger.common.KryoFactory;
import ch.hslu.vsk.logger.common.dataobject.LogMessageDo;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
 *          .targetsServer(URI.create("localhost:9999"))
 *          .build();
 *  client.info("This is an info-level message");
 * </pre>
 */
public class LoggerClient implements LoggerSetup {
    private LogLevel minLogLevel;
    private Socket socket;
    private Output output;
    private final Kryo kryo;
    private final URI targetServerAddress;
    private ScheduledFuture<?> reconnectFuture;
    private final String source;
    private final ScheduledExecutorService scheduler;
    private final LogCacher logCacher;
    private boolean isReconnecting;

    protected LoggerClient(final LoggerClientBuilder builder) {
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.minLogLevel = builder.getMinLogLevel();
        this.source = builder.getSource();
        this.logCacher = new LogCacher(builder.getFallbackFile());
        this.targetServerAddress = builder.getTargetServerAddress();
        kryo = KryoFactory.createConfiguredKryoInstance();

        try {
            this.socket = new Socket(targetServerAddress.getHost(), targetServerAddress.getPort());
            this.output = new Output(socket.getOutputStream());
            this.logCacher.sendCachedLogs(this::sendLog);
        } catch (IOException ioException) {
            this.tryToReconnect();
        }
    }

    /**
     * Sends a log to the remote target.
     *
     * @param message The message to send.
     * @param level   The LogLevel of the message.
     */
    public void sendLog(final String message, final LogLevel level) {
        LogMessageDo messageDo = new LogMessageDo.Builder(message)
                .from(source)
                .at(Instant.now())
                .level(level)
                .build();

        this.sendLog(messageDo);
    }

    private void sendLog(final LogMessageDo messageDo) {
        if (messageDo.getLevel().compareTo(this.minLogLevel) > 0) {
            return; // do not log if log level is below minimum level
        }

        try {
            kryo.writeObject(output, messageDo);
            output.flush();
        } catch (Exception e) {
            this.tryToReconnect();
            this.logCacher.cache(messageDo);
        }
    }

    @SuppressWarnings("EmptyCatchBlock")
    private void tryToReconnect() {
        if (!this.isReconnecting) {
            this.isReconnecting = true;
            this.reconnectFuture = this.scheduler.scheduleAtFixedRate(() -> {
                        try {
                            this.socket = new Socket(targetServerAddress.getHost(), targetServerAddress.getPort());
                            this.output = new Output(socket.getOutputStream());
                            this.logCacher.sendCachedLogs(this::sendLog);
                            this.isReconnecting = false;
                            this.reconnectFuture.cancel(false);
                        } catch (IOException ignored) {
                        }
                    },
                    0,
                    500,
                    TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Creates an Instance of the RemoteLogger.
     *
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
}
