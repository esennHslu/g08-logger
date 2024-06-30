package ch.hslu.vsk.logger.viewer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.net.URISyntaxException;

import ch.hslu.vsk.logger.LoggerViewer;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

final class LoggerViewerTest {

    private LoggerViewer loggerViewer;

    @Mock
    private WebSocketClient client;

    private URI uri;
    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    /**
     * Initializes the JavaFX toolkit before running any tests.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    @BeforeAll
    public static void setUpClass() throws InterruptedException {
        JFXTestUtil.initToolkit();
    }

    /**
     * Sets up the test environment before each test case.
     * Initializes mocks and sets up the LoggerViewer instance with a mock WebSocketClient.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    @BeforeEach
    public void setUp() throws InterruptedException {
        MockitoAnnotations.openMocks(this);
        Platform.runLater(() -> {
            loggerViewer = new LoggerViewer() {
                @Override
                public WebSocketClient createWebSocketClient(URI uri) {
                    return client; // Return the mock client
                }
            };
            Stage stage = new Stage();
            loggerViewer.start(stage); // Start the LoggerViewer application
        });
        this.uri = URI.create("ws://localhost:8025");
        JFXTestUtil.waitForRunLater();
    }

    /**
     * Tests that the LoggerViewer correctly displays a log message.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    @Test
    public void testDisplayLogMessage() throws InterruptedException {
        Platform.runLater(() -> {
            loggerViewer.displayLogMessage("Test message");
            VBox logContainer = loggerViewer.getLogContainer();
            Label logLabel = (Label) logContainer.getChildren().get(0);
            assertEquals("Test message", logLabel.getText());
        });
        JFXTestUtil.waitForRunLater();
    }

    /**
     * Tests that the LoggerViewer correctly displays the connection status.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    @Test
    public void testDisplayConnectionStatus() throws InterruptedException {
        Platform.runLater(() -> {
            loggerViewer.displayConnectionStatus("Connected to server");
            VBox logContainer = loggerViewer.getLogContainer();
            Label statusLabel = (Label) logContainer.getChildren().get(0);
            assertEquals("Connected to server", statusLabel.getText());
        });
        JFXTestUtil.waitForRunLater();
    }

    /**
     * Tests that the LoggerViewer connects to the server correctly.
     * @throws URISyntaxException if the URI is incorrect.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    @Test
    public void testConnectToServer() throws URISyntaxException, InterruptedException {
        LoggerViewer spyLoggerViewer = spy(loggerViewer);
        doReturn(client).when(spyLoggerViewer).createWebSocketClient(any());

        Platform.runLater(spyLoggerViewer::connectToServer);
        JFXTestUtil.waitForRunLater();
        verify(client, times(1)).connect(); // Verify that connect() was called exactly once
    }

    /**
     * Tests that the LoggerViewer handles the WebSocket onOpen event correctly.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    @Test
    public void testOnOpen() throws InterruptedException {
        when(client.getURI()).thenReturn(this.uri);

        Platform.runLater(() -> {
            WebSocketClient webSocketClient = loggerViewer.createWebSocketClient(this.uri);
            webSocketClient.onOpen(mock(ServerHandshake.class));
        });
        JFXTestUtil.waitForRunLater();
        verify(client, times(1)).connect(); // Verify that connect() was called exactly once
    }

    /**
     * Tests that the LoggerViewer handles the WebSocket onClose event correctly.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    @Test
    public void testOnClose() throws InterruptedException {
        when(client.getURI()).thenReturn(this.uri);

        Platform.runLater(() -> {
            WebSocketClient webSocketClient = loggerViewer.createWebSocketClient(this.uri);
            webSocketClient.onClose(1000, "Test close", false);
        });
        JFXTestUtil.waitForRunLater();
        verify(client, times(1)).connect(); // Verify that connect() was called exactly once
    }

    /**
     * Tests that the LoggerViewer handles the WebSocket onError event correctly.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    @Test
    public void testOnError() throws InterruptedException {
        when(client.getURI()).thenReturn(this.uri);

        Platform.runLater(() -> {
            WebSocketClient webSocketClient = loggerViewer.createWebSocketClient(this.uri);
            webSocketClient.onError(new Exception("Test error"));
        });
        JFXTestUtil.waitForRunLater();
        verify(client, times(1)).connect(); // Verify that connect() was called exactly once
    }
}
