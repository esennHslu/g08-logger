package ch.hslu.vsk.logger.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Properties;

public final class ConfigReader {

    private final Properties properties;

    public ConfigReader() {

        try (InputStream propsInput = getClass().getResourceAsStream("/LoggerServer.properties")) {
            properties = new Properties();
            properties.load(propsInput);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Config file not found: " + e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getLogFilePath() { return getEnvVariableOrConfig("LOG_FILE"); }

    public Integer getSocketPort() { return Integer.valueOf(getEnvVariableOrConfig("LISTEN_PORT")); }

    public String getSocketAddress() { return getEnvVariableOrConfig("SOCKET_ADDRESS"); }

    public String getCustomSetting(final String settingName) {
        if (properties.containsKey(settingName)) {
            return properties.getProperty(settingName);
        }

        throw new NoSuchElementException("Found no setting with name " + settingName);
    }

    private String getEnvVariableOrConfig(final String key) {
        String value = System.getenv(key);
        if (value != null) {
            return value;
        }
        return properties.getProperty(key);
    }
}
