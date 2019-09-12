package fi.livi.digitraffic.tie.conf;

import java.io.File;
import java.nio.file.FileStore;

import org.apache.commons.io.input.ReversedLinesFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@ConditionalOnNotWebApplication
@Component
public class LoggerDebugScheduler {
    private static final Logger log = LoggerFactory.getLogger(LoggerDebugScheduler.class);

    @Autowired
    public  LoggerDebugScheduler() {
        log.info("Started log files tail debugger");
    }

    @Scheduled(fixedDelayString = "60000")
    public void debugLog() {
        try {
            File tmp = new File("/logs");
            log.info("mount free space: " + (tmp.getFreeSpace() / 1024 / 1024) + "M");
        } catch (Exception e) {
            log.warn("mount check failed", e);
        }

        try (ReversedLinesFileReader rReader = new ReversedLinesFileReader(new File("logs/road-daemon-daily.log"))) {
            log.info("last line: {}", rReader.readLine());
        } catch (Exception e) {
            log.warn("log check failed", e);
        }
    }
}
