package fi.livi.digitraffic.tie.conf;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

//@Configuration
public class SchedulingConfiguration implements SchedulingConfigurer {
    @Override
    public void configureTasks(final ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
    }

    @Bean(destroyMethod="shutdown")
    public ScheduledExecutorService taskExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }
}
