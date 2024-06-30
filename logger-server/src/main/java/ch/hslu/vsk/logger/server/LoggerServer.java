package ch.hslu.vsk.logger.server;

import ch.hslu.vsk.logger.common.KryoFactory;
import ch.hslu.vsk.logger.server.adapter.FileStringPersistorLogAdapter;
import ch.hslu.vsk.logger.server.adapter.LogAdapter;
import ch.hslu.vsk.logger.server.adapter.LoggerViewerLogAdapter;
import ch.hslu.vsk.logger.server.logstrategies.CompetitionStrategy;
import ch.hslu.vsk.stringpersistor.FileStringPersistor;
import ch.hslu.vsk.stringpersistor.api.StringPersistor;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.util.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The {@code LoggerServer} class encapsulates a simple TCP server that listens for log messages
 * on a specified port. When a log message is received, it prints the message to the console.
 * This server demonstrates a basic usage of Java's {@code ServerSocket} for accepting connections
 * and reading objects sent to it over an Object stream.
 */
public final class LoggerServer {
    private static final Logger LOG = LoggerFactory.getLogger(LoggerServer.class);
    private final ConfigReader config;
    private final List<LogAdapter> logAdapters;
    private final LogWebSocketServer logWebSocketServer;
    private ServerSocket listener;
    private ExecutorService virtualThreadExecutor;
    private final Pool<Kryo> kryoPool;

    /**
     * Constructs a new {@code LoggerServer} instance while injecting and configuring its dependencies.
     *
     * @param config             Reader in order to resolve configuration properties.
     * @param logAdapters        Collection of adapters used to handle incoming log messages
     * @param logWebSocketServer Server instance used to broadcast incoming log messages to all subscribed viewers
     * @param kryoPool           Pool for obtaining {@link Kryo} instances
     * @throws IllegalArgumentException if one of the arguments is {@code null}
     */
    public LoggerServer(final ConfigReader config,
                        final List<LogAdapter> logAdapters,
                        final LogWebSocketServer logWebSocketServer,
                        final Pool<Kryo> kryoPool) {
        if (config == null) {
            throw new IllegalArgumentException("Provided config reader cannot be null");
        }
        if (logAdapters == null) {
            throw new IllegalArgumentException("Provided log adapters cannot be null");
        }
        if (logWebSocketServer == null) {
            throw new IllegalArgumentException("Provided logWebSocketServer cannot be null");
        }
        if (kryoPool == null) {
            throw new IllegalArgumentException("Provided kryo pool cannot be null");
        }

        this.config = config;
        this.logAdapters = logAdapters;
        this.logWebSocketServer = logWebSocketServer;
        this.kryoPool = kryoPool;
    }

    /**
     * Starts the server which attempts to listen on the socket with the config specified port.
     * Upon successful connection on port, listens for incoming connections and delegates the further handling.
     *
     * @throws IllegalStateException if the leasing of the socket failed or the server is in an invalid,
     *                               non-recoverable state and thus to be terminated.
     */
    @SuppressWarnings("InfiniteLoopStatement")
    public void listen() {
        try {
            logWebSocketServer.startServer();
            listener = new ServerSocket(config.getSocketPort(), 0,
                    InetAddress.getByName(config.getSocketAddress()));
            virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

            LOG.info("Server started, listening on {}:{} for connections...",
                    config.getSocketAddress(),
                    config.getSocketPort());

            while (true) {
                Socket client = listener.accept();
                Kryo kryo = kryoPool.obtain();
                Runnable logConsumer = new LogMessageRequestHandler(client, logAdapters, kryo);
                virtualThreadExecutor.execute(logConsumer);
            }
        } catch (UnknownHostException unknownHostException) {
            LOG.error(String.format("Failed to resolve host during startup, for hostname: %s",
                            config.getSocketAddress()),
                    unknownHostException);
            throw new IllegalStateException("Failed to listen for incoming connections", unknownHostException);
        } catch (IOException ioException) {
            LOG.error(String.format("Failed to connect to socket on port: %d", config.getSocketPort()),
                    ioException.getMessage());
            throw new IllegalStateException("Failed to listen for incoming connections", ioException);
        } catch (Exception exception) {
            LOG.error("Something unexpected went wrong", exception);
            throw new IllegalStateException("Unexpected error during runtime", exception);
        }
    }

    /**
     * Stops the server and releases resources.
     */
    public void stop() {
        try {
            if (listener != null && !listener.isClosed()) {
                listener.close();
            }
            if (virtualThreadExecutor != null && !virtualThreadExecutor.isShutdown()) {
                virtualThreadExecutor.shutdown();
            }
            logWebSocketServer.stopServer();
            LOG.info("Server stopped");
        } catch (IOException e) {
            LOG.error("Error stopping the server", e);
        }
    }

    /**
     * The entry point for the server application.
     * Creates an instance of {@code LoggerServer} and starts it.
     *
     * @param args The command-line arguments for the application (not used).
     */
    public static void main(final String[] args) {
        ConfigReader configReader = new ConfigReader();
        LogStrategy logStrategy = new CompetitionStrategy();
        StringPersistor stringPersistor = new FileStringPersistor();
        stringPersistor.setFile(Path.of(configReader.getLogFilePath()));
        LogWebSocketServer logWebSocketServer = new LogWebSocketServer(configReader.getLoggerViewerSocketPort());
        FileStringPersistorLogAdapter fileStringPersistorLogAdapter = new FileStringPersistorLogAdapter(stringPersistor, logStrategy);
        LoggerViewerLogAdapter loggerViewerLogAdapter = new LoggerViewerLogAdapter(logStrategy, logWebSocketServer);
        List<LogAdapter> adapters = Arrays.asList(fileStringPersistorLogAdapter, loggerViewerLogAdapter);
        Pool<Kryo> kryoPool = new Pool<>(false, true) {
            @Override
            protected Kryo create() {
                return KryoFactory.createConfiguredKryoInstance();
            }
        };
        LoggerServer server = new LoggerServer(configReader, adapters, logWebSocketServer, kryoPool);

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        server.listen();
    }
}
