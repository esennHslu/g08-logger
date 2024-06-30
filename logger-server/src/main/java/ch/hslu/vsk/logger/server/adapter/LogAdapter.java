package ch.hslu.vsk.logger.server.adapter;

import ch.hslu.vsk.logger.common.dataobject.LogMessageDo;

/**
 * Interface for any adapter which pushes a log message to another component and or system.
 */
public interface LogAdapter {
    /**
     * Saves the given message to the adapted component in an appropriate form
     *
     * @param messageDo Log message which should be saved
     */
    void saveLogMessage(final LogMessageDo messageDo);
}
