package ch.hslu.vsk.logger.viewer;

import javafx.application.Platform;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class JFXTestUtil {
    private static final CountDownLatch latch = new CountDownLatch(1);

    public static void initToolkit() throws InterruptedException {
        Platform.startup(() -> {
            // Initialize JavaFX toolkit
        });
        latch.countDown();
    }

    public static void waitForRunLater() throws InterruptedException {
        CountDownLatch runLaterLatch = new CountDownLatch(1);
        Platform.runLater(runLaterLatch::countDown);
        runLaterLatch.await(10, TimeUnit.SECONDS);
    }
}
