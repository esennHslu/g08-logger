package ch.hslu.vsk.logger.server;

import ch.hslu.vsk.logger.common.dataobject.LogMessageDo;
import ch.hslu.vsk.stringpersistor.FileStringPersistor;
import ch.hslu.vsk.stringpersistor.api.StringPersistor;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Path;
import java.time.Instant;

/**
 * The {@code LoggerServer} class encapsulates a simple TCP server that listens for log messages
 * on a specified port. When a log message is received, it prints the message to the console.
 * This server demonstrates a basic usage of Java's {@code ServerSocket} for accepting connections
 * and reading objects sent to it over an Object stream.
 */
public class LoggerServer {
    private final ServerSocket serverSocket;
    private final StringPersistor stringPersistor = new FileStringPersistor();
    private final ConfigReader config = new ConfigReader();

    /**
     * Constructs a new {@code LoggerServer} that listens on the specified port.
     *
     * @throws Exception If an I/O error occurs when opening the socket.
     */
    public LoggerServer() throws Exception {
        serverSocket = new ServerSocket(config.getSocketPort());
        stringPersistor.setFile(this.getLogfilePath());
    }

    /**
     * Calculates the path depending if the config contains absolute or relative path.
     * Mainly used during development can be removed once dockerization is finished (always absolute path).
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
     * Starts the server, which enters an infinite loop, listening for and processing incoming connections.
     * For each connection, it reads a {@code LogMessage} object and prints its message to the console.
     * This method demonstrates handling of client socket connections and object input streams.
     */
    @SuppressWarnings("InfiniteLoopStatement")
    public void start() {
        System.out.println("Server started, waiting for connections...");

        try (Socket clientSocket = serverSocket.accept()) {
            try (ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream())) {
                while (true) {
                    LogMessageDo messageDo = (LogMessageDo) inputStream.readObject();
                    this.logMessage(messageDo);
                }
            } catch (EOFException e) {
                System.out.println("Client closed the connection");
            } catch (SocketException e) {
                System.out.println("SocketException: Possible client forceful termination or network issue. Message: "
                        + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logMessage(final LogMessageDo messageDo) {
        var msg = String.format("[%s, %s]: %s", messageDo.getSource(), messageDo.getCreatedAt(), messageDo.getMessage());
        stringPersistor.save(Instant.now(), msg);
    }

    /**
     * The entry point for the server application.
     * Creates an instance of {@code LoggerServer} and starts it.
     * The port number is retrieved from a shared constant {@code SocketConnection.SOCKET_PORT}.
     *
     * @param args The command-line arguments for the application (not used).
     * @throws Exception If an error occurs starting the server.
     */
    public static void main(final String[] args) throws Exception {
        LoggerServer server = new LoggerServer();
        server.start();
    }
}
