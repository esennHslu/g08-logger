package ch.hslu.vsk.logger.server;

import ch.hslu.vsk.logger.adapter.LogMessageAdapter;
import ch.hslu.vsk.logger.common.dataobject.LogMessageDo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.time.Instant;

/**
 * Dedicated runnable for consuming all log messages sent by one client (one socket connection) and persisting them.
 */
public final class LogMessageRequestHandler implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(LogMessageRequestHandler.class);
    private final Socket client;
    private final LogMessageAdapter logAdapter;

    /**
     * Constructs a new {@link LogMessageRequestHandler} instance, while injecting its dependencies.
     *
     * @param client     Established socket connection to the client
     * @param logAdapter Adapter for persisting received log messages
     * @throws IllegalArgumentException if one of the arguments is {@code null}
     */
    public LogMessageRequestHandler(final Socket client, final LogMessageAdapter logAdapter) {
        if (client == null) {
            throw new IllegalArgumentException("Provided client cannot be null");
        }
        if (logAdapter == null) {
            throw new IllegalArgumentException("Provided log adapter cannot be null");
        }

        this.client = client;
        this.logAdapter = logAdapter;
    }

    /**
     * Receives and persists all log messages sent over the socket connection indefinitely until connection is closed.
     * Connection may be closed due to client cancellation, network issues etc.
     */
    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        LOG.info("Connected to: {}", client);
        try (ObjectInputStream inputStream = new ObjectInputStream(client.getInputStream())) {
            while (true) {
                LogMessageDo messageDo = (LogMessageDo) inputStream.readObject();
                Instant receivedLogAt = Instant.now();
                messageDo = registerProcessedAt(messageDo, receivedLogAt);
                logAdapter.saveLogMessage(messageDo);
            }
        } catch (ClassNotFoundException classNotFoundException) {
            LOG.error("Failed to deserialize log message", classNotFoundException);
        } catch (EOFException e) {
            LOG.error("Client closed the connection");
        } catch (SocketException e) {
            LOG.error("SocketException: Possible client forceful termination or network issue", e);
        } catch (IOException ioException) {
            LOG.error("Something went wrong during receiving the log message or deserializing it", ioException);
        } finally {
            if (!client.isClosed()) {
                // Try to close socket gracefully
                try {
                    client.close();
                } catch (Exception exception) {
                    LOG.error(String.format("Failed to close client socket: %s", client), exception);
                }
            }
            LOG.info("Connection closed for: {}", client);
        }
    }

    private LogMessageDo registerProcessedAt(final LogMessageDo message, final Instant timestamp) {
        // Return new DO instance hence its instances are immutable
        return new LogMessageDo.Builder(message.getMessage())
                .from(message.getSource())
                .level(message.getLevel())
                .at(message.getCreatedAt())
                .processed(timestamp)
                .build();
    }
}
