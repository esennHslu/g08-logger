package ch.hslu.vsk.logger.adapter;

import ch.hslu.vsk.logger.common.dataobject.LogMessageDo;
import ch.hslu.vsk.logger.server.LogStrategy;
import ch.hslu.vsk.stringpersistor.api.StringPersistor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The LogMessageAdapter class is responsible for saving log messages to a persistor using a strategy.
 */
public class LogMessageAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(LogMessageAdapter.class);
    private final StringPersistor stringPersistor;
    private final LogStrategy strategy;

    /**
     * Constructs a new LogMessageAdapter instance.
     *
     * @param stringPersistor The persistor to use for saving log messages
     * @param strategy        The strategy to use for formatting log messages
     */
    public LogMessageAdapter(final StringPersistor stringPersistor, final LogStrategy strategy) {
        this.stringPersistor = stringPersistor;
        this.strategy = strategy;
    }

    /**
     * Saves a log message to the configured persistor.
     *
     * @param messageDo The log message to save
     */
    public void saveLogMessage(final LogMessageDo messageDo) {
        try {
            var msg = this.strategy.format(messageDo);
            this.stringPersistor.save(messageDo.getProcessedAt(), msg);
            LOG.debug(messageDo.toString()); // be decoupled from used strategy for integration tests
        } catch (Exception e) {
            LOG.error("Error saving log message");
        }
    }
}
