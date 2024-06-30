package ch.hslu.vsk.logger.server.adapter;

import ch.hslu.vsk.logger.common.dataobject.LogMessageDo;
import ch.hslu.vsk.logger.server.LogStrategy;
import ch.hslu.vsk.logger.server.LogWebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Propagates the given log messages to the running {@link LogWebSocketServer} instance in order for it to display on
 * all subscribed viewers.
 *
 * @author esenn
 */
public class LoggerViewerLogAdapter implements LogAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(LoggerViewerLogAdapter.class);
    private final LogWebSocketServer logWebSocketServer;
    private final LogStrategy strategy;

    /**
     * Constructs a new LoggerViewerLogAdapter instance.
     *
     * @param strategy           The strategy to use for formatting log messages
     * @param logWebSocketServer The WebSocket server to use for broadcasting log messages
     */
    public LoggerViewerLogAdapter(final LogStrategy strategy, final LogWebSocketServer logWebSocketServer) {
        this.strategy = strategy;
        this.logWebSocketServer = logWebSocketServer;
    }

    /**
     * Saves a log message to the configured persistor.
     *
     * @param messageDo The log message to save
     */
    public void saveLogMessage(final LogMessageDo messageDo) {
        try {
            var msg = this.strategy.format(messageDo);
            logWebSocketServer.broadcast(msg);
            LOG.debug(messageDo.toString()); // be decoupled from used strategy for integration tests
        } catch (Exception e) {
            LOG.error("Failed to propagate log to log ws server", e);
        }
    }
}
