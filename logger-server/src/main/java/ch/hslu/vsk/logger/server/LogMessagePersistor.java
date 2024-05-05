package ch.hslu.vsk.logger.server;

import ch.hslu.vsk.logger.common.dataobject.LogMessageDo;
import ch.hslu.vsk.stringpersistor.api.StringPersistor;

import java.util.concurrent.BlockingQueue;

/**
 * Dedicated runnable which polls until cancelled indefinitely from a log-queue while persisting all
 * polled log messages.
 */
public final class LogMessagePersistor implements Runnable {
    private final BlockingQueue<LogMessageDo> logPipeline;
    private final LogStrategy logStrategy;
    private final StringPersistor stringPersistor;

    /**
     * Constructs a new {@link LogMessagePersistor} instance, while injecting its dependencies.
     *
     * @param logPipeline     Initialized blocking queue for polling messages to be persisted
     * @param logStrategy     Log format to use for serializing log-messages
     * @param stringPersistor Component for persisting serialized log messages
     * @throws IllegalArgumentException if one of the arguments is {@code null}
     */
    public LogMessagePersistor(final BlockingQueue<LogMessageDo> logPipeline,
                               final LogStrategy logStrategy,
                               final StringPersistor stringPersistor) {
        if (logPipeline == null) {
            throw new IllegalArgumentException("Provided logPipeline cannot be null");
        }
        if (logStrategy == null) {
            throw new IllegalArgumentException("Provided log strategy cannot be null");
        }
        if (stringPersistor == null) {
            throw new IllegalArgumentException("Provided string-persistor cannot be null");
        }

        this.logPipeline = logPipeline;
        this.logStrategy = logStrategy;
        this.stringPersistor = stringPersistor;
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
                String serializedMessage = logStrategy.format(messageDo);
                stringPersistor.save(messageDo.getProcessedAt(), serializedMessage);
                System.out.println(serializedMessage);
            }
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }
}
