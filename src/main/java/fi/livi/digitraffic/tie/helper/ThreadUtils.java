package fi.livi.digitraffic.tie.helper;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadUtils {
    private static final Logger log = LoggerFactory.getLogger(ThreadUtils.class);

    final static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static void delayMs(final long delayMs) {

        log.info("Start waiting for {} ms", delayMs);
        final ScheduledFuture<?> future = scheduler.schedule(() ->
                log.info("Completed waiting {}", delayMs), delayMs, TimeUnit.MILLISECONDS);
        try {
            future.get();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (final ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


}
