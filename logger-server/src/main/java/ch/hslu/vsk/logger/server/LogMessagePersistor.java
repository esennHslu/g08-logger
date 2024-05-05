package ch.hslu.vsk.logger.server;

import java.util.concurrent.BlockingQueue;

import ch.hslu.vsk.logger.adapter.LogMessageAdapter;
import ch.hslu.vsk.logger.common.dataobject.LogMessageDo;

/**
 * Dedicated runnable which polls until cancelled indefinitely from a log-queue while persisting all
 * polled log messages.
 */
public final class LogMessagePersistor implements Runnable {
    private final BlockingQueue<LogMessageDo> logPipeline;
    private final LogMessageAdapter logMessageAdapter;

    /**
     * Constructs a new {@link LogMessagePersistor} instance, while injecting its dependencies.
     *
     * @param logPipeline Initialized blocking queue for polling messages to be persisted
     * @param logMessageAdapter Adapter for persisting log messages
     * @throws IllegalArgumentException if one of the arguments is {@code null}
     */
    public LogMessagePersistor(final BlockingQueue<LogMessageDo> logPipeline,
                               final LogMessageAdapter logMessageAdapter) {
        if (logPipeline == null) {
            throw new IllegalArgumentException("Provided logPipeline cannot be null");
        }
        if (logMessageAdapter == null) {
            throw new IllegalArgumentException("Provided logMessageAdapter cannot be null");
        }

        this.logPipeline = logPipeline;
        this.logMessageAdapter = logMessageAdapter;
    }

    /**
     * Continuously polls for new log messages in the injected queue and persists them using the given string-persistor.
     */
    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        try {
            while (true) {
                LogMessageDo messageDo = logPipeline.take();
                logMessageAdapter.saveLogMessage(messageDo);
            }
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }
}
