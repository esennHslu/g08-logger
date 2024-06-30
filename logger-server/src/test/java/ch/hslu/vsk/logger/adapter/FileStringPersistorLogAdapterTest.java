package ch.hslu.vsk.logger.adapter;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.common.dataobject.LogMessageDo;
import ch.hslu.vsk.logger.server.LogStrategy;
import ch.hslu.vsk.logger.server.adapter.FileStringPersistorLogAdapter;
import ch.hslu.vsk.stringpersistor.api.StringPersistor;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author esenn
 */
public class FileStringPersistorLogAdapterTest {
    @Test
    public void testSaveLogMessage() {
        StringPersistor mockPersistor = mock(StringPersistor.class);
        LogStrategy mockStrategy = mock(LogStrategy.class);

        FileStringPersistorLogAdapter adapter = new FileStringPersistorLogAdapter(mockPersistor, mockStrategy);
        Instant createdInstant = Instant.parse("2007-12-03T10:15:30Z");
        Instant processedInstant = Instant.parse("2007-12-03T10:20:30Z");
        LogMessageDo messageDo = new LogMessageDo.Builder("test")
                .from("source")
                .at(createdInstant)
                .processed(processedInstant)
                .level(LogLevel.Info)
                .build();

        // Set up the strategy mock to return a specific format
        String expectedMessage = "[Info | source, 2007-12-03T10:15:30Z]: test";
        when(mockStrategy.format(messageDo)).thenReturn(expectedMessage);

        adapter.saveLogMessage(messageDo);

        // Verify that the save method was called with the correct parameters
        verify(mockPersistor).save(eq(processedInstant), eq(expectedMessage));
    }


    @Test
    public void testDifferentLogLevels() {
        StringPersistor mockPersistor = mock(StringPersistor.class);
        LogStrategy mockStrategy = mock(LogStrategy.class);
        FileStringPersistorLogAdapter adapter = new FileStringPersistorLogAdapter(mockPersistor, mockStrategy);
        Instant createdInstant = Instant.parse("2007-12-03T10:15:30Z");
        Instant processedInstant = Instant.parse("2007-12-03T10:20:30Z");
        LogLevel[] levels = LogLevel.values();
        for (LogLevel level : levels) {
            LogMessageDo messageDo = new LogMessageDo.Builder("test")
                    .from("source")
                    .at(createdInstant)
                    .processed(processedInstant)
                    .level(level)
                    .build();
            String expectedMessage = "[" + level + "]: message";
            when(mockStrategy.format(messageDo)).thenReturn(expectedMessage);
            adapter.saveLogMessage(messageDo);
            verify(mockPersistor).save(any(Instant.class), eq(expectedMessage));
        }
    }

    @Test
    public void testEmptyMessage() {
        StringPersistor mockPersistor = mock(StringPersistor.class);
        LogStrategy mockStrategy = mock(LogStrategy.class);
        FileStringPersistorLogAdapter adapter = new FileStringPersistorLogAdapter(mockPersistor, mockStrategy);
        Instant createdInstant = Instant.parse("2007-12-03T10:15:30Z");
        Instant processedInstant = Instant.parse("2007-12-03T10:20:30Z");
        LogMessageDo emptyMessageDo = new LogMessageDo.Builder("")
                .from("source")
                .at(createdInstant)
                .processed(processedInstant)
                .level(LogLevel.Info)
                .build();
        String expectedMessage = "[]: ";
        when(mockStrategy.format(emptyMessageDo)).thenReturn(expectedMessage);
        adapter.saveLogMessage(emptyMessageDo);
        verify(mockPersistor).save(any(Instant.class), eq(expectedMessage));
    }

    @Test
    public void testExceptionHandlingInSaveMethod() {
        StringPersistor mockPersistor = mock(StringPersistor.class);
        LogStrategy mockStrategy = mock(LogStrategy.class);
        FileStringPersistorLogAdapter adapter = new FileStringPersistorLogAdapter(mockPersistor, mockStrategy);
        Instant fixedInstant = Instant.parse("2007-12-03T10:15:30Z");

        LogMessageDo messageDo = new LogMessageDo.Builder("test")
                .from("source")
                .at(fixedInstant)
                .level(LogLevel.Info)
                .build();
        when(mockStrategy.format(messageDo)).thenReturn("formatted message");
        doThrow(new RuntimeException("Persistence failed")).when(mockPersistor).save(any(), anyString());

        assertDoesNotThrow(() -> adapter.saveLogMessage(messageDo));
    }
}
