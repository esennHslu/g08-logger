package ch.hslu.vsk.logger.server;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.common.dataobject.LogMessageDo;
import ch.hslu.vsk.logger.server.logstrategies.CompetitionStrategy;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objenesis.strategy.SerializingInstantiatorStrategy;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@Testcontainers
final class LoggerServerIT {
    @Container
    private final GenericContainer<?> server = new GenericContainer<>(
            new ImageFromDockerfile("ad-hoc/logger-server", true)
                    .withFileFromFile("target/g08-loggerserver.jar", new File("target/g08-loggerserver.jar"))
                    .withDockerfileFromBuilder(builder -> builder
                            .from("eclipse-temurin:21-jre")
                            .expose(9999)
                            .workDir("/app")
                            .cmd("java", "-jar", "g08-loggerserver.jar")
                            .copy("./target/g08-loggerserver.jar", "/app/")
                            .build()))
            .withExposedPorts(9999)
            .waitingFor(Wait.forLogMessage(".*Server started, listening on.*\n", 1));

    @Test
    void testServerStartup() {
        // Arrange
        String startupSuccessful = String.format("Server started, listening on 0.0.0.0:%d for connections...",
                server.getExposedPorts().getFirst());

        // Act
        String logs = server.getLogs();

        // Assert
        assertThat(logs).contains(startupSuccessful);
    }

    @Test
    void testClientCanSendLogToServer() {
        // Arrange
        try (Socket connection = new Socket(server.getHost(), server.getFirstMappedPort());
             Output output = new Output(connection.getOutputStream())) {
            var message = new LogMessageDo.Builder("test message")
                    .at(Instant.now())
                    .level(LogLevel.Info)
                    .from("test-client")
                    .build();
            String expectedLog = message.toString();

            // Act
            Kryo kryo = createKryo();
            kryo.writeObject(output, message);
            output.flush();
            Thread.sleep(50); // grace period in order to allow server to process request

            // Assert
            String logs = server.getLogs();
            assertThat(logs).contains(expectedLog);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void testClientsLogWillBePersistedInLogFile(@TempDir Path tempDir) {
        // Arrange
        LogStrategy usedStrategy = new CompetitionStrategy();
        try (Socket connection = new Socket(server.getHost(), server.getFirstMappedPort());
             Output output = new Output(connection.getOutputStream())) {
            Instant fixed = Instant.parse("2024-05-01T10:10:00.00Z");
            var message = new LogMessageDo.Builder("test message")
                    .at(fixed)
                    .level(LogLevel.Info)
                    .from("test-client")
                    .build();
            String expectedLog = usedStrategy.format(message);

            // Act
            Kryo kryo = createKryo();
            kryo.writeObject(output, message);
            output.flush();
            Thread.sleep(100); // grace period in order to allow server to persist log

            // Assert
            String logs = retrieveLogFileFromContainer(tempDir);
            assertThat(logs).contains(expectedLog);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void testClientDisconnectWithoutCrash() {
        try {
            // Arrange
            Socket connection = new Socket(server.getHost(), server.getFirstMappedPort());

            // Act
            Thread.sleep(50); // slight delay to prevent hiccup
            connection.close();
            Thread.sleep(50); // grace period for io-channel close etc.

            // Assert
            String logs = server.getLogs();
            assertThat(logs).contains("Connection closed");
            assertThat(server.isRunning()).isTrue();
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void testMultipleClientsLogSimultaneously(@TempDir Path tempDir) {
        // Arrange
        int clientAmount = 3;
        int logAmount = 500;
        CountDownLatch logSentCountdown = new CountDownLatch(clientAmount * logAmount);
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            // Act
            for (int i = 0; i < clientAmount; i++) {
                int clientNo = i;
                executor.execute(() -> {
                    Kryo kryo = createKryo(); // create own instance for each thread

                    try (
                            Socket connection = new Socket(server.getHost(), server.getFirstMappedPort());
                            Output output = new Output(connection.getOutputStream())) {
                        for (int j = 0; j < logAmount; j++) {
                            var message = new LogMessageDo.Builder(String.format("log-%d", j))
                                    .from(String.format("client-%d", clientNo))
                                    .at(Instant.now())
                                    .level(LogLevel.Info)
                                    .build();

                            kryo.writeObject(output, message);
                            output.flush();
                            logSentCountdown.countDown();
                            Thread.sleep(50);
                        }
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                });
            }

            logSentCountdown.await();

            // Assert
            String logs = retrieveLogFileFromContainer(tempDir);
            for (int i = 0; i < clientAmount; i++) {
                for (int j = 0; j < logAmount; j++) {
                    // TODO use strategy independent checking if all logs made it to the file
                    //      requires the extension of the LogStrategy interface in order to be able to deserialize
                    //      the logs again
                    assertThat(logs).contains(String.format("client-%d log-%d", i, j));
                }
            }
        } catch (Exception e) {
            fail(e);
        }
    }

    private String retrieveLogFileFromContainer(Path targetDir) throws IOException {
        Path localLogFile = Path.of(targetDir.toString(), "Logger.log");
        server.copyFileFromContainer("/app/Logs/Logger.log", localLogFile.toString());
        return Files.readString(localLogFile, StandardCharsets.UTF_8);
    }

    private Kryo createKryo() {
        var kryo = new Kryo();
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new SerializingInstantiatorStrategy()));
        kryo.register(Instant.class, 1);
        kryo.register(LogLevel.class, 2);
        kryo.register(LogMessageDo.class, 3);
        return kryo;
    }
}
