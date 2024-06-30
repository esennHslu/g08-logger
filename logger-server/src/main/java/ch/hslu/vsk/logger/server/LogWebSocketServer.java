package ch.hslu.vsk.logger.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * LogWebSocketServer is a WebSocket server that broadcasts log messages
 * to connected clients.
 */
public class LogWebSocketServer extends WebSocketServer {
    private static final Logger LOG = LoggerFactory.getLogger(LogMessageRequestHandler.class);
    private static final Set<WebSocket> clients = Collections.synchronizedSet(new HashSet<>());
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Constructs a LogWebSocketServer that listens on the specified port.
     *
     * @param port the port number on which the server will listen.
     */
    public LogWebSocketServer(int port) {
        super(new InetSocketAddress(port));
    }

    /**
     * Called when a new WebSocket connection is established.
     *
     * @param conn      the WebSocket connection.
     * @param handshake the handshake data.
     */
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        clients.add(conn);
        LOG.info("New connection from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    /**
     * Called when a WebSocket connection is closed.
     *
     * @param conn   the WebSocket connection.
     * @param code   the closing code.
     * @param reason the reason for closing.
     * @param remote whether the closing was initiated by the remote peer.
     */
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        clients.remove(conn);
        LOG.info("Closed connection to " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    /**
     * Called when a message is received from a client.
     * This server does not process received messages.
     *
     * @param conn    the WebSocket connection.
     * @param message the received message.
     */
    @Override
    public void onMessage(WebSocket conn, String message) {
        // This server only sends messages, it doesn't process received messages.
    }

    /**
     * Called when an error occurs.
     *
     * @param conn the WebSocket connection, if available.
     * @param ex   the exception that occurred.
     */
    @Override
    public void onError(WebSocket conn, Exception ex) {
        LOG.error("An error occurred on connection " + conn.getRemoteSocketAddress().getAddress().getHostAddress(), ex);
    }

    /**
     * Called when the server has successfully started.
     */
    @Override
    public void onStart() {
        running.set(true);
        LOG.info("WebSocket server started successfully");
    }

    /**
     * Stops the WebSocket server.
     *
     * @param timeout the timeout in milliseconds to wait for existing connections to close.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    @Override
    public void stop(int timeout) throws InterruptedException {
        super.stop(timeout);
        running.set(false);
        LOG.info("WebSocket server stopped successfully");
    }

    /**
     * Broadcasts a log message to all connected clients.
     *
     * @param logMessage the log message to broadcast.
     */
    public void broadcast(String logMessage) {
        synchronized (clients) {
            for (WebSocket client : clients) {
                if (client.isOpen()) {
                    client.send(logMessage);
                }
            }
        }
    }

    /**
     * Starts the WebSocket server.
     * Ensures that the server is started only once.
     */
    public void startServer() {
        if (running.compareAndSet(false, true)) {
            try {
                this.start();
                LOG.info("WebSocket server started on port " + getPort());
            } catch (Exception e) {
                running.set(false);
                LOG.error("Failed to start WebSocket server", e);
            }
        }
    }

    /**
     * Stops the WebSocket server.
     * Ensures that the server is stopped only once.
     */
    public void stopServer() {
        if (running.compareAndSet(true, false)) {
            try {
                this.stop(1000);
                LOG.info("WebSocket server stopped");
            } catch (InterruptedException e) {
                running.set(true);
                LOG.error("Failed to stop WebSocket server", e);
            }
        }
    }

}
