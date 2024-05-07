package ch.hslu.vsk.logger.common.dataobject;

import ch.hslu.vsk.logger.api.LogLevel;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(beforeWeight).isLessThan(0);
        assertThat(afterWeight).isGreaterThan(0);
        assertThat(equalWeight).isEqualTo(0);
    }

    @Test
    public void testToStringFormat() {
        // Arrange
        Instant fixed = Instant.parse("2024-05-01T10:10:00.00Z");
        String expectedStr = String.format("[%s] [Debug] test-app: test message", fixed);
        var message = new LogMessageDo.Builder("test message")
                .from("test-app")
                .level(LogLevel.Debug)
                .at(fixed)
                .build();

        // Act
        String messageStr = message.toString();

        // Assert
        assertThat(messageStr).isEqualTo(expectedStr);
    }
}
