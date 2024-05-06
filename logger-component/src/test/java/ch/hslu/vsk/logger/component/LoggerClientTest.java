package ch.hslu.vsk.logger.component;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.api.LoggerSetup;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class LoggerClientTest {
    @Test
    public void testSettingMinLogLevelDebug() {
        LogLevel expectedLevel = LogLevel.Debug;
        LoggerSetup client = new LoggerClientBuilder()
                .requires(LogLevel.Debug)
                .targetsServer(URI.create("http://localhost:9999"))
                .usesAsFallback(Path.of("Test"))
                .build();

        assertEquals(expectedLevel, client.getMinLogLevel(), "MinLogLevel should be same");
    }

    @Test
    public void testSettingMinLogLevelInfo() {
        LogLevel expectedLevel = LogLevel.Info;
        LoggerSetup client = new LoggerClientBuilder()
                .requires(LogLevel.Info)
                .targetsServer(URI.create("http://localhost:9999"))
                .usesAsFallback(Path.of("Test"))
                .build();

        assertEquals(expectedLevel, client.getMinLogLevel(), "MinLogLevel should be same");
    }

    @Test
    public void testSettingMinLogLevelWarning() {
        LogLevel expectedLevel = LogLevel.Warning;
        LoggerSetup client = new LoggerClientBuilder()
                .requires(LogLevel.Warning)
                .targetsServer(URI.create("http://localhost:9999"))
                .usesAsFallback(Path.of("Test"))
                .build();

        assertEquals(expectedLevel, client.getMinLogLevel(), "MinLogLevel should be same");
    }

    @Test
    public void testSettingMinLogLevelError() {
        LogLevel expectedLevel = LogLevel.Error;
        LoggerSetup client = new LoggerClientBuilder()
                .requires(LogLevel.Error)
                .targetsServer(URI.create("http://localhost:9999"))
                .usesAsFallback(Path.of("Test"))
                .build();

        assertEquals(expectedLevel, client.getMinLogLevel(), "MinLogLevel should be same");
    }

    @Test
    public void testNotSettingTargetServerThrowingException() {
        assertThrows(NullPointerException.class, () -> {
            new LoggerClientBuilder()
                    .requires(LogLevel.Info)
                    .usesAsFallback(Path.of("Test"))
                    .build();
        }, "Expected NullPointerException when targetServer not set");
    }
}
