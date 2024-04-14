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

    public String getLogFilePath() {
        return properties.getProperty("LOGFILE_PATH");
    }

    public Integer getSocketPort() {
        return Integer.valueOf(properties.getProperty("SOCKET_PORT"));
    }

    public String getSocketAddress() {
        return properties.getProperty("SOCKET_ADDRESS");
    }

    public String getCustomSetting(final String settingName) {
        if (properties.containsKey(settingName)) {
            return properties.getProperty(settingName);
        }

        throw new NoSuchElementException("Found no setting with name " + settingName);
    }
}
