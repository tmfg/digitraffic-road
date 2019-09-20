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
        try (ReversedLinesFileReader rReader = new ReversedLinesFileReader(new File("logs/road-daemon-daily.log"))) {
            log.info("last line: {}", rReader.readLine());
        } catch (Exception e) {
            log.warn("log check failed", e);
        }

        try {
            File tmp = new File("logs");

            StringBuilder builder = new StringBuilder();

            long total = loopDirectory(tmp, builder);

            builder.append("mount free space: " + (tmp.getFreeSpace() / 1024 / 1024) + "M, logs: " + (total  / 1024 / 1024) + "M");

            log.info(builder.toString());
        } catch (Exception e) {
            log.warn("mount check failed", e);
        }
    }

    private long loopDirectory(File file, StringBuilder builder) {
        long size = 0;

        if (file.isDirectory()) {
            for (File innerFile : file.listFiles()) {
                if (innerFile.isFile()) {
                    size += append(file, innerFile, builder);
                } else if (innerFile.isDirectory()) {
                    size += loopDirectory(innerFile, builder);
                }
            }
        } else {
            size += append(null, file, builder);
        }

        return size;
    }

    private long append(File parent, File file, StringBuilder builder) {
        if (parent != null) {
            builder.append(parent.getName());
            builder.append("/");
        }

        builder.append(file.getName());
        builder.append(": ");
        builder.append(file.length());
        builder.append("\n");

        return file.length();
    }
}
