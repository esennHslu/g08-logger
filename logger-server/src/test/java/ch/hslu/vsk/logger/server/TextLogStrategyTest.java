package ch.hslu.vsk.logger.server;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.common.dataobject.LogMessageDo;
import ch.hslu.vsk.logger.server.logstrategies.TextLogStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

public class TextLogStrategyTest {
    @Test
    void testFormatTextDefault() {
        //arrange
        LogStrategy strategy = new TextLogStrategy();

        Instant fixedInstant = Instant.parse("2007-12-03T10:15:30Z");

        LogMessageDo messageDo = new LogMessageDo.Builder("test")
                .from("source")
                .at(fixedInstant)
                .level(LogLevel.Info)
                .build();

        String expected = "[2007-12-03T10:15:30Z] [Info] source: test";

        //act
        String formatted = strategy.format(messageDo);

        //assert
        Assertions.assertEquals(expected, formatted);
    }
}
