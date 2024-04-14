package ch.hslu.vsk.logger.component;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.api.LoggerSetup;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class LoggerClientTest {
    @Test
    public void testSettingMinLogLevelDebug() {
        LogLevel expectedLevel = LogLevel.Debug;
        LoggerSetup client = new LoggerClient.Builder()
                .requires(LogLevel.Debug)
                .targetsServer(URI.create("http://localhost:9999"))
                .build();

        assertEquals(expectedLevel, client.getMinLogLevel(), "MinLogLevel should be same");
    }

    @Test
    public void testSettingMinLogLevelInfo() {
        LogLevel expectedLevel = LogLevel.Info;
        LoggerSetup client = new LoggerClient.Builder()
                .requires(LogLevel.Info)
                .targetsServer(URI.create("http://localhost:9999"))
                .build();

        assertEquals(expectedLevel, client.getMinLogLevel(), "MinLogLevel should be same");
    }

    @Test
    public void testSettingMinLogLevelWarning() {
        LogLevel expectedLevel = LogLevel.Warning;
        LoggerSetup client = new LoggerClient.Builder()
                .requires(LogLevel.Warning)
                .targetsServer(URI.create("http://localhost:9999"))
                .build();

        assertEquals(expectedLevel, client.getMinLogLevel(), "MinLogLevel should be same");
    }

    @Test
    public void testSettingMinLogLevelError() {
        LogLevel expectedLevel = LogLevel.Error;
        LoggerSetup client = new LoggerClient.Builder()
                .requires(LogLevel.Error)
                .targetsServer(URI.create("http://localhost:9999"))
                .build();

        assertEquals(expectedLevel, client.getMinLogLevel(), "MinLogLevel should be same");
    }

    @Test
    public void testNotSettingTargetServerThrowingException() {
        assertThrows(NullPointerException.class, () -> {
            new LoggerClient.Builder()
                    .requires(LogLevel.Info)
                    .build();
        }, "Expected NullPointerException when targetServer not set");
    }
}
