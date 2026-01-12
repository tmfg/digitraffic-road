package fi.livi.digitraffic.tie.conf;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@EnableAsync
public class AsyncExecutorConfiguration implements AsyncConfigurer {

    private static final Logger log = LoggerFactory.getLogger(AsyncExecutorConfiguration.class);

    @Override
    public Executor getAsyncExecutor() {
        final int cores = Runtime.getRuntime().availableProcessors();
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(cores);
        executor.setMaxPoolSize(cores * 2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("AsyncExecutor-");
        executor.setRejectedExecutionHandler(
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        log.info("method=getAsyncExecutor Initialized AsyncExecutor with corePoolSize={}, maxPoolSize={}, queueCapacity={}, threadNamePrefix={}",
                executor.getCorePoolSize(),
                executor.getMaxPoolSize(),
                executor.getQueueCapacity(),
                executor.getThreadNamePrefix());
        return executor;
    }

    @Override
    public @Nullable AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncUncaughtExceptionHandler() {
            private static final Logger log = getLogger(AsyncExecutorConfiguration.class);

            @Override
            public void handleUncaughtException(
                    final @NonNull Throwable ex,
                    final @NonNull Method method,
                    final Object @NonNull ... params) {
                log.error("method=handleUncaughtException Unexpected exception occurred invoking async method: {} params: {}", method, params, ex);
            }
        };
    }
}
