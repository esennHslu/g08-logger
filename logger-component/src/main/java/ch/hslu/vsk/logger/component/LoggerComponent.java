package ch.hslu.vsk.logger.component;

import ch.hslu.vsk.logger.common.SocketConnection;
import ch.hslu.vsk.logger.common.dataobject.LogMessageDo;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.Instant;

/**
 * Represents a component responsible for logging messages by sending them over a socket connection.
 */
public class LoggerComponent {
    // TODO: replace with dedicated management / configuration for the source name
    private static final String TEST_APP_SOURCE = "demo-app";

    private final Socket socket;
    private final ObjectOutputStream outputStream;

    /**
     * Constructs a new LoggerComponent, establishing a connection to the logging server.
     *
     * @throws Exception if an error occurs while establishing the connection
     */
    public LoggerComponent() throws Exception {
        this.socket = new Socket(SocketConnection.SOCKET_ADDRESS, SocketConnection.SOCKET_PORT);
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
    }

    /**
     * Sends a log message over the established socket connection.
     *
     * @param message the message to be logged
     * @throws Exception if an error occurs while sending the log message
     */
    public void sendLog(final String message) throws Exception {
        Instant timestamp = Instant.now(); // TODO: move to logger implementation (as soon as implemented)

        LogMessageDo messageDo = new LogMessageDo.Builder(message)
                .from(TEST_APP_SOURCE)
                .at(timestamp)
                .build();

        outputStream.writeObject(messageDo);
        outputStream.flush();
    }

    /**
     * Closes the socket connection and output stream.
     *
     * @throws Exception if an error occurs while closing the connection
     */
    public void close() throws Exception {
        outputStream.close();
        socket.close();
    }
}
