package ch.hslu.vsk.logger.server;

import ch.hslu.vsk.logger.adapter.LogMessageAdapter;
import ch.hslu.vsk.logger.common.dataobject.LogMessageDo;
import ch.hslu.vsk.logger.server.logstrategies.TextLogStrategy;
import ch.hslu.vsk.stringpersistor.FileStringPersistor;
import ch.hslu.vsk.stringpersistor.api.StringPersistor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * The {@code LoggerServer} class encapsulates a simple TCP server that listens for log messages
 * on a specified port. When a log message is received, it prints the message to the console.
 * This server demonstrates a basic usage of Java's {@code ServerSocket} for accepting connections
 * and reading objects sent to it over an Object stream.
 */
public final class LoggerServer {
    private static final Logger LOG = LoggerFactory.getLogger(LoggerServer.class);
    private final ConfigReader config;
    private final LogMessageAdapter logMessageAdapter;

    /**
     * Constructs a new {@code LoggerServer} instance while injecting and configuring its dependencies.
     *
     * @param config            Reader in order to resolve configuration properties
     * @param logMessageAdapter Adapter for persisting log messages
     * @throws IllegalArgumentException if one of the arguments is {@code null}
     */
    public LoggerServer(final ConfigReader config, final LogMessageAdapter logMessageAdapter) {
        if (config == null) {
            throw new IllegalArgumentException("Provided config reader cannot be null");
        }
        if (logMessageAdapter == null) {
            throw new IllegalArgumentException("Provided log-message-adapter cannot be null");
        }

        this.config = config;
        this.logMessageAdapter = logMessageAdapter;
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
        BlockingQueue<LogMessageDo> logPipeline = new PriorityBlockingQueue<>();

        try (ServerSocket listener = new ServerSocket(config.getSocketPort(), 0,
                InetAddress.getByName(config.getSocketAddress()));
             ExecutorService persistorExecutor = Executors.newSingleThreadExecutor();
             ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            Runnable logPersistor = new LogMessagePersistor(logPipeline, logMessageAdapter);
            persistorExecutor.execute(logPersistor);

            LOG.info("Server started, listening on {}:{} for connections...",
                    config.getSocketAddress(),
                    config.getSocketPort());

            while (true) {
                Socket client = listener.accept();
                Runnable logConsumer = new LogMessageConsumer(client, logPipeline);
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
     * The entry point for the server application.
     * Creates an instance of {@code LoggerServer} and starts it.
     *
     * @param args The command-line arguments for the application (not used).
     */
    public static void main(final String[] args) {
        ConfigReader configReader = new ConfigReader();
        LogStrategy logStrategy = new TextLogStrategy();
        StringPersistor stringPersistor = new FileStringPersistor();
        stringPersistor.setFile(Path.of(configReader.getLogFilePath()));
        LogMessageAdapter logMessageAdapter = new LogMessageAdapter(stringPersistor, logStrategy);

        LoggerServer server = new LoggerServer(configReader, logMessageAdapter);
        server.listen();
    }
}
