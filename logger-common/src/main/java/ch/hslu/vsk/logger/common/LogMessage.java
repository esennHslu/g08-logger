package ch.hslu.vsk.logger.common;

import java.io.Serializable;

/**
 * A serializable interface representing a log message.
 */
public interface LogMessage extends Serializable {

    /**
     * Retrieves the message associated with this log entry.
     *
     * @return the message associated with this log entry
     */
    String getMessage();
}
