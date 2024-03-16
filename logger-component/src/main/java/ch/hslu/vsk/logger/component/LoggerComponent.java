package ch.hslu.vsk.logger.component;

import ch.hslu.vsk.logger.common.LogMessage;
import ch.hslu.vsk.logger.common.LogMessageFactory;
import ch.hslu.vsk.logger.common.SocketConnection;

import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Represents a component responsible for logging messages by sending them over a socket connection.
 */
public class LoggerComponent {
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
        LogMessage logMessage = LogMessageFactory.createLogMessage(message);
        outputStream.writeObject(logMessage);
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
