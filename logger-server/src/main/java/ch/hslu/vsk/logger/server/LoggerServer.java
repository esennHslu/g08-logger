package ch.hslu.vsk.logger.server;

import ch.hslu.vsk.logger.adapter.LogMessageAdapter;
import ch.hslu.vsk.logger.common.dataobject.LogMessageDo;
import ch.hslu.vsk.logger.server.logstrategies.TextLogStrategy;
import ch.hslu.vsk.stringpersistor.FileStringPersistor;
import ch.hslu.vsk.stringpersistor.api.StringPersistor;

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
    private final StringPersistor stringPersistor;
    private final LogStrategy strategy;
    private final ConfigReader config;
    private final LogMessageAdapter logMessageAdapter;

    /**
     * Constructs a new {@code LoggerServer} instance while injecting and configuring its dependencies.
     *
     * @param config          Reader in order to resolve configuration properties
     * @param logStrategy     Log format to use for serializing log-messages
     * @param stringPersistor Component for persisting log-messages
     * @throws IllegalArgumentException if one of the arguments is {@code null}
     */
    public LoggerServer(final ConfigReader config,
                        final LogStrategy logStrategy,
                        final StringPersistor stringPersistor,
                        final LogMessageAdapter logMessageAdapter) {
        if (config == null) {
            throw new IllegalArgumentException("Provided config reader cannot be null");
        }
        if (logStrategy == null) {
            throw new IllegalArgumentException("Provided log strategy cannot be null");
        }
        if (stringPersistor == null) {
            throw new IllegalArgumentException("Provided string-persistor cannot be null");
        }
        if (logMessageAdapter == null) {
            throw new IllegalArgumentException("Provided log-message-adapter cannot be null");
        }

        this.config = config;
        this.strategy = logStrategy;
        this.stringPersistor = stringPersistor;
        this.stringPersistor.setFile(getLogfilePath());
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

            System.out.printf("Server started, listening on %s:%d for connections...%n",
                    config.getSocketAddress(),
                    config.getSocketPort());

            while (true) {
                Socket client = listener.accept();
                Runnable logConsumer = new LogMessageConsumer(client, logPipeline);
                virtualThreadExecutor.execute(logConsumer);
            }
        } catch (UnknownHostException unknownHostException) {
            System.err.printf("Failed to resolve host during startup, for hostname: %s, due to: %s%n",
                    config.getSocketAddress(),
                    unknownHostException.getMessage());
            throw new IllegalStateException("Failed to listen for incoming connections", unknownHostException);
        } catch (IOException ioException) {
            System.err.printf("Failed to connect to socket on port: %d, due to: %s%n",
                    config.getSocketPort(),
                    ioException.getMessage());
            throw new IllegalStateException("Failed to listen for incoming connections", ioException);
        } catch (Exception exception) {
            System.err.printf("Something unexpected went wrong, reason: %s%n", exception.getMessage());
            throw new IllegalStateException("Unexpected error during runtime", exception);
        }
    }

    /**
     * Calculates the path depending on if the config contains absolute or relative path.
     * Mainly used during development can be removed once dockerization is finished (always absolute path).
     *
     * @return Path to logfile.
     */
    private Path getLogfilePath() {
        var logPath = Path.of(config.getLogFilePath());
        if (logPath.isAbsolute()) {
            return logPath;
        }
        return Path.of(System.getProperty("user.dir"), logPath.toString());
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
        LogMessageAdapter logMessageAdapter = new LogMessageAdapter(stringPersistor, logStrategy);

        LoggerServer server = new LoggerServer(configReader, logStrategy, stringPersistor, logMessageAdapter);
        server.listen();
    }
}
