package ch.hslu.vsk.logger;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * LoggerViewer is a JavaFX application that connects to a WebSocket server
 * to display log messages in real-time.
 */
public class LoggerViewer extends Application {
    private static final Logger LOG = LoggerFactory.getLogger(LoggerViewer.class);

    private WebSocketClient client;
    private VBox logContainer;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final AtomicBoolean isConnecting = new AtomicBoolean(false);
    private final AtomicBoolean isReconnecting = new AtomicBoolean(false);

    private static String domain = "localhost";
    private static int port = 8025;

    /**
     * The main entry point for all JavaFX applications.
     *
     * @param primaryStage the primary stage for this application, onto which
     *                     the application scene can be set.
     */
    @Override
    public void start(Stage primaryStage) {
        logContainer = new VBox();
        ScrollPane scrollPane = new ScrollPane(logContainer);
        Scene scene = new Scene(scrollPane, 800, 600);

        connectToServer();

        primaryStage.setScene(scene);
        primaryStage.setTitle("LoggerViewer");
        primaryStage.show();
    }

    /**
     * Establishes a connection to the WebSocket server.
     * It ensures that only one connection attempt is made at a time.
     */
    public void connectToServer() {
        if (isConnecting.compareAndSet(false, true)) {
            try {
                client = createWebSocketClient(new URI(String.format("ws://%s:%d", domain, port)));
                client.connect();
            } catch (URISyntaxException e) {
                isConnecting.set(false);
                LOG.error("Could not connect to Server: Invalid URI", e);
            }
        }
    }

    /**
     * Creates a WebSocketClient to connect to the given URI.
     *
     * @param uri the URI to connect to.
     * @return the created WebSocketClient.
     */
    public WebSocketClient createWebSocketClient(URI uri) {
        return new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                isConnected.set(true);
                isConnecting.set(false);
                isReconnecting.set(false);
                Platform.runLater(() -> displayConnectionStatus("Connected to server"));
            }

            @Override
            public void onMessage(String message) {
                Platform.runLater(() -> displayLogMessage(message));
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                isConnected.set(false);
                isConnecting.set(false);
                Platform.runLater(() -> displayConnectionStatus("Connection lost: " + reason));
                reconnectToServer();
            }

            @Override
            public void onError(Exception ex) {
                isConnected.set(false);
                isConnecting.set(false);
                Platform.runLater(() -> displayConnectionStatus("Error: " + ex.getMessage()));
                reconnectToServer();
            }
        };
    }

    /**
     * Sets the WebSocketClient for testing purposes.
     *
     * @param client the WebSocketClient to set.
     */
    protected void setClient(WebSocketClient client) {
        this.client = client;
    }

    /**
     * Attempts to reconnect to the WebSocket server after a connection is lost.
     * It ensures that only one reconnection attempt is made at a time.
     */
    private void reconnectToServer() {
        if (!isConnected.get() && !isConnecting.get() && isReconnecting.compareAndSet(false, true)) {
            new Thread(() -> {
                try {
                    Platform.runLater(() -> displayConnectionStatus("Reconnecting..."));
                    Thread.sleep(5000);
                    connectToServer();
                } catch (InterruptedException e) {
                    LOG.error("Reconnection attempt interrupted", e);
                } finally {
                    isReconnecting.set(false);
                }
            }).start();
        }
    }

    /**
     * Displays a log message in the application window.
     *
     * @param message the log message to display.
     */
    public void displayLogMessage(String message) {
        Label logLabel = new Label(message);
        logContainer.getChildren().add(logLabel);
    }

    /**
     * Displays the connection status in the application window.
     *
     * @param status the connection status to display.
     */
    public void displayConnectionStatus(String status) {
        Label statusLabel = new Label(status);
        logContainer.getChildren().add(statusLabel);
    }

    /**
     * Called when the application is stopped. Closes the WebSocket connection.
     */
    @Override
    public void stop() {
        if (client != null) {
            client.close();
        }
    }

    /**
     * The main method for launching the JavaFX application.
     *
     * @param args the command line arguments.
     */
    public static void main(String[] args) {
        // Parse command-line arguments for domain and port
        if (args.length >= 2) {
            domain = args[0];
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                LOG.error("Invalid port number: " + args[1], e);
            }
        }
        launch(args);
    }

    /**
     * Returns the log container for testing purposes.
     *
     * @return the log container.
     */
    public VBox getLogContainer() {
        return logContainer;
    }
}
