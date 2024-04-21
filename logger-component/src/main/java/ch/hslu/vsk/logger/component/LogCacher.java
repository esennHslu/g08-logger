package ch.hslu.vsk.logger.component;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.common.dataobject.LogMessageDo;
import ch.hslu.vsk.stringpersistor.FileStringPersistor;
import ch.hslu.vsk.stringpersistor.api.PersistedString;
import ch.hslu.vsk.stringpersistor.api.StringPersistor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.function.Consumer;

public final class LogCacher {

    private final StringPersistor stringPersistor;
    private final Path fallbackfilePath;
    private int cachedLogs;
    private int reSentLogs;

    public LogCacher(final Path fallbackFile) {
        this.fallbackfilePath = fallbackFile;
        this.stringPersistor = new FileStringPersistor();
        this.stringPersistor.setFile(fallbackFile);
        this.cachedLogs = this.stringPersistor.get(Integer.MAX_VALUE).size();
    }

    public void sendCachedLogs(final Consumer<LogMessageDo> logSender) {
        if (this.cachedLogs == this.reSentLogs) {
            return;
        }

        this.stringPersistor
                .get(Integer.MAX_VALUE)
                .stream()
                .skip(this.reSentLogs)
                .map(LogCacher::buildLogMessageDo)
                .forEach((log) -> {
                    logSender.accept(log);
                    reSentLogs++;
                });

        // Delete Cache file to not resend messages after restart
        if (this.cachedLogs == this.reSentLogs) {
            try {
                Files.delete(this.fallbackfilePath);
                this.cachedLogs = 0;
                this.reSentLogs = 0;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static LogMessageDo buildLogMessageDo(final PersistedString persistedString) {
        var infos = persistedString.getPayload().split(";", 3);
        return new LogMessageDo.Builder(infos[2])
                .level(LogLevel.valueOf(infos[0]))
                .from(infos[1])
                .at(persistedString.getTimestamp())
                .build();
    }

    public void cache(final LogMessageDo messageDo) {
        String log = MessageFormat.format("{0};{1};{2}",
                messageDo.getLevel(),
                messageDo.getSource(),
                messageDo.getMessage());
        this.stringPersistor.save(Instant.now(), log);
        this.cachedLogs++;
    }
}
