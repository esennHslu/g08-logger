/*
 * Copyright 2024 Roland Gisler, HSLU Informatik, Switzerland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.hslu.vsk.logger.component;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import ch.hslu.vsk.logger.api.LogLevel;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Testcases for {@code LoggerComponent}.
 */
final class RemoteLoggerTest {
    @Test
    public void testLogMessage() throws Exception {
        // Arrange
        LoggerClient client = mock(LoggerClient.class);
        RemoteLogger logger = new RemoteLogger(client);

        String message = "Test msg";
        LogLevel level = LogLevel.Info;

        // Act
        logger.log(level, message);

        // Assert
        verify(client).sendLog(message, level);
    }

    @Test
    public void testDebug() throws Exception {
        // Arrange
        LoggerClient client = mock(LoggerClient.class);
        RemoteLogger logger = new RemoteLogger(client);

        String message = "Test msg";
        LogLevel level = LogLevel.Debug;

        // Act
        logger.debug(message);

        // Assert
        verify(client).sendLog(message, level);
    }

    @Test
    public void testInfo() throws Exception {
        // Arrange
        LoggerClient client = mock(LoggerClient.class);
        RemoteLogger logger = new RemoteLogger(client);

        String message = "Test msg";
        LogLevel level = LogLevel.Info;

        // Act
        logger.info(message);

        // Assert
        verify(client).sendLog(message, level);
    }

    @Test
    public void testWarning() throws Exception {
        // Arrange
        LoggerClient client = mock(LoggerClient.class);
        RemoteLogger logger = new RemoteLogger(client);

        String message = "Test msg";
        LogLevel level = LogLevel.Warning;

        // Act
        logger.warn(message);

        // Assert
        verify(client).sendLog(message, level);
    }

    @Test
    public void testErrorWithoutException() throws Exception {
        // Arrange
        LoggerClient client = mock(LoggerClient.class);
        RemoteLogger logger = new RemoteLogger(client);

        String message = "Test msg";
        LogLevel level = LogLevel.Error;

        // Act
        logger.error(message);

        // Assert
        verify(client).sendLog(message, level);
    }

    @Test
    public void testErrorWithException() throws Exception {
        // Arrange
        LoggerClient client = mock(LoggerClient.class);
        RemoteLogger logger = new RemoteLogger(client);

        String message = "Test msg";
        IllegalArgumentException e = new IllegalArgumentException("test exception");
        LogLevel level = LogLevel.Error;

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        String expectedLogMessage = message + sw.toString();

        // Act
        logger.error(message, e);

        // Assert
        verify(client).sendLog(expectedLogMessage, level);
    }
}
