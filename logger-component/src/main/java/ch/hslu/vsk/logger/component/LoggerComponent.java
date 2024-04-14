package ch.hslu.vsk.logger.component;

import ch.hslu.vsk.logger.common.dataobject.LogMessageDo;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.Instant;

/**
 * Represents a component responsible for logging messages by sending them over a socket connection.
 */
public class LoggerComponent {
    private final Socket socket;
    private final ObjectOutputStream outputStream;

    // Remove SOCKET_PORT & SOCKET_ADDRESS & CLIENT_NAME and use info from Interface
    public static final int SOCKET_PORT = 9999;
    public static final String SOCKET_ADDRESS = "localhost";
    public static final String CLIENT_NAME = "demo-app";

    /**
     * Constructs a new LoggerComponent, establishing a connection to the logging server.
     *
     * @throws Exception if an error occurs while establishing the connection
     */
    public  LoggerComponent() throws Exception {
        this.socket = new Socket(SOCKET_ADDRESS, SOCKET_PORT);
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
    }

    /**
     * Sends a log message over the established socket connection.
     *
     * @param message the message to be logged
     * @throws Exception if an error occurs while sending the log message
     */
    public void sendLog(final String message) throws Exception {
        Instant timestamp = Instant.now(); // Get info from interface
        var a = 1;
        LogMessageDo messageDo = new LogMessageDo.Builder(message)
                .from(CLIENT_NAME)
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
