package ch.hslu.vsk.logger.common.dataobject;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public final class LogMessageDoTest {
    @Test
    public void testEqualsContract() {
        EqualsVerifier.forClass(LogMessageDo.class).verify();
    }
}
