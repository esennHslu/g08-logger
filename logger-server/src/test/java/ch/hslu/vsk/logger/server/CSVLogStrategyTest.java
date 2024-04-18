package ch.hslu.vsk.logger.server;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.common.dataobject.LogMessageDo;
import ch.hslu.vsk.logger.server.logstrategies.CSVLogStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

public class CSVLogStrategyTest {
    @Test
    void testFormatCSVDefault() {
        //arrange
        LogStrategy strategy = new CSVLogStrategy();

        Instant fixedInstant = Instant.parse("2007-12-03T10:15:30Z");

        LogMessageDo messageDo = new LogMessageDo.Builder("test")
                .from("source")
                .at(fixedInstant)
                .level(LogLevel.Info)
                .build();

        String expected = "2007-12-03T10:15:30Z, Info, source, test";

        //act
        String formatted = strategy.format(messageDo);

        //assert
        Assertions.assertEquals(expected, formatted);
    }

    @Test
    void testFormatCSVDWithCommasInMessage() {
        //arrange
        LogStrategy strategy = new CSVLogStrategy();

        Instant fixedInstant = Instant.parse("2007-12-03T10:15:30Z");

        LogMessageDo messageDo = new LogMessageDo.Builder("hello, my name is jeff")
                .from("source")
                .at(fixedInstant)
                .level(LogLevel.Info)
                .build();

        String expected = "2007-12-03T10:15:30Z, Info, source, \"hello, my name is jeff\"";

        //act
        String formatted = strategy.format(messageDo);

        //assert
        Assertions.assertEquals(expected, formatted);
    }

    @Test
    void testFormatCSVDWithQuotesInMessage() {
        //arrange
        LogStrategy strategy = new CSVLogStrategy();

        Instant fixedInstant = Instant.parse("2007-12-03T10:15:30Z");

        LogMessageDo messageDo = new LogMessageDo.Builder("hello my \"name is jeff")
                .from("source")
                .at(fixedInstant)
                .level(LogLevel.Info)
                .build();

        String expected = "2007-12-03T10:15:30Z, Info, source, \"hello my \"\"name is jeff\"";

        //act
        String formatted = strategy.format(messageDo);

        //assert
        Assertions.assertEquals(expected, formatted);
    }

    @Test
    void testFormatCSVDWithCommasAndQuotesInMessage() {
        //arrange
        LogStrategy strategy = new CSVLogStrategy();

        Instant fixedInstant = Instant.parse("2007-12-03T10:15:30Z");

        LogMessageDo messageDo = new LogMessageDo.Builder("hello, my \"name is jeff")
                .from("source")
                .at(fixedInstant)
                .level(LogLevel.Info)
                .build();

        String expected = "2007-12-03T10:15:30Z, Info, source, \"hello, my \"\"name is jeff\"";

        //act
        String formatted = strategy.format(messageDo);

        //assert
        Assertions.assertEquals(expected, formatted);
    }
}
