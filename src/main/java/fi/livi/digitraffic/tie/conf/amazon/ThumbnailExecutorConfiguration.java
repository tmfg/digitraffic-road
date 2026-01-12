package fi.livi.digitraffic.tie.conf.amazon;

import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

import static org.slf4j.LoggerFactory.getLogger;

@Configuration
public class ThumbnailExecutorConfiguration {

    private static final Logger log = getLogger(ThumbnailExecutorConfiguration.class);

    @Bean
    public ThreadPoolTaskExecutor thumbnailExecutor() {
        final int cores = Runtime.getRuntime().availableProcessors();
        // Use number of available processors as pool size when generating thumbnails as it is
        // CPU-bound as Doing pure computation and not waiting on io etc.
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(cores);
        executor.setMaxPoolSize(cores); // Or cores + 1
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("ThumbnailResizeExecutor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        log.info("method=thumbnailExecutor Initialized AsyncExecutor with corePoolSize={}, maxPoolSize={}, queueCapacity={}, threadNamePrefix={}",
                executor.getCorePoolSize(),
                executor.getMaxPoolSize(),
                executor.getQueueCapacity(),
                executor.getThreadNamePrefix());
        return executor;
    }
}
