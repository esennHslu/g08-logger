package ch.hslu.vsk.logger.server;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.common.dataobject.LogMessageDo;
import ch.hslu.vsk.logger.server.logstrategies.CompetitionStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

public class CompetitionStrategyTest {
    @Test
    public void ProvidedExampleTest() {
        LogStrategy strategy = new CompetitionStrategy();

        Instant fixedInstant = Instant.parse("2024-04-29T16:43:16.2345Z");

        LogMessageDo messageDo = new LogMessageDo.Builder("Example LogMessage")
                .from("MyHost")
                .at(fixedInstant)
                .level(LogLevel.Info)
                .build();

        String expected = "2024-04-29 16:43:16.2345 INFO MyHost Example LogMessage";

        //act
        String formatted = strategy.format(messageDo);

        //assert
        Assertions.assertEquals(expected, formatted);
    }
}
