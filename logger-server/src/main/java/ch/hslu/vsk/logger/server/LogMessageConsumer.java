package ch.hslu.vsk.logger.server;

import ch.hslu.vsk.logger.common.dataobject.LogMessageDo;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;

/**
 * Dedicated runnable for consuming all log messages sent by one client (one socket connection) and forwarding them to
 * be persisted later on.
 */
public final class LogMessageConsumer implements Runnable {
    private final Socket client;
    private final BlockingQueue<LogMessageDo> logPipeline;

    /**
     * Constructs a new {@link LogMessageConsumer} instance, while injecting its dependencies.
     *
     * @param client      Established socket connection to the client
     * @param logPipeline Initialized blocking queue for forwarding the received log messages
     * @throws IllegalArgumentException if one of the arguments is {@code null}
     */
    public LogMessageConsumer(final Socket client, final BlockingQueue<LogMessageDo> logPipeline) {
        if (client == null) {
            throw new IllegalArgumentException("Provided client cannot be null");
        }
        if (logPipeline == null) {
            throw new IllegalArgumentException("Provided logPipeline cannot be null");
        }

        this.client = client;
        this.logPipeline = logPipeline;
    }

    /**
     * Receives and forwards all log messages sent over the socket connection indefinitely until connection is closed.
     * Connection may be closed due to client cancellation, network issues etc.
     */
    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        System.out.printf("Connected to: %s%n", client);
        try (ObjectInputStream inputStream = new ObjectInputStream(client.getInputStream())) {
            while (true) {
                LogMessageDo messageDo = (LogMessageDo) inputStream.readObject();
                Instant receivedLogAt = Instant.now();
                messageDo = registerProcessedAt(messageDo, receivedLogAt);
                try {
                    logPipeline.put(messageDo);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (ClassNotFoundException classNotFoundException) {
            System.err.printf("Failed to deserialize log message, reason: %s%n", classNotFoundException.getMessage());
        } catch (EOFException e) {
            System.err.println("Client closed the connection");
        } catch (SocketException e) {
            System.err.printf("SocketException: Possible client forceful termination or network issue, reason: %s%n",
                    e.getMessage());
        } catch (IOException ioException) {
            System.err.printf("Something went wrong during receiving the log message or deserializing it, reason: %s%n",
                    ioException.getMessage());
        } finally {
            if (!client.isClosed()) {
                // Try to close socket gracefully
                try {
                    client.close();
                } catch (Exception exception) {
                    System.err.printf("Failed to close client socket: %s, reason: %s",
                            client,
                            exception.getMessage());
                }
            }
            System.out.printf("Connection closed for: %s%n", client);
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
