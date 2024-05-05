package ch.hslu.vsk.logger.common.dataobject;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

public final class LogMessageDoTest {
    @Test
    public void testEqualsContract() {
        EqualsVerifier.forClass(LogMessageDo.class).verify();
    }

    @Test
    public void testComparableContractComparesCreatedAtAsc() {
        // Arrange
        LogMessageDo referenceMessage = new LogMessageDo.Builder("test")
                .at(Instant.parse("2024-05-01T10:10:00.00Z"))
                .build();
        LogMessageDo messageAfter = new LogMessageDo.Builder("before")
                .at(Instant.parse("2024-05-01T10:11:00.00Z"))
                .build();
        LogMessageDo messageBefore = new LogMessageDo.Builder("before")
                .at(Instant.parse("2024-05-01T10:09:00.00Z"))
                .build();
        LogMessageDo messageEqual = new LogMessageDo.Builder("before")
                .at(Instant.parse("2024-05-01T10:10:00.00Z"))
                .build();

        // Act
        int beforeWeight = referenceMessage.compareTo(messageAfter);
        int afterWeight = referenceMessage.compareTo(messageBefore);
        int equalWeight = referenceMessage.compareTo(messageEqual);

        // Assert
        Assertions.assertTrue(beforeWeight < 0);
        Assertions.assertTrue(afterWeight > 0);
        Assertions.assertEquals(0, equalWeight);
    }
}
