package fi.livi.digitraffic.tie.conf;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import fi.livi.digitraffic.tie.aop.PerformanceMonitorAspect;
import fi.livi.digitraffic.tie.aop.ScheduleJobLogger;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Aspect
public class AopConfiguration {

    @Bean
    public PerformanceMonitorAspect performanceMonitorAspect() {
        return new PerformanceMonitorAspect();
    }

    @Bean
    public ScheduleJobLogger scheduleJobLogger() {
        return new ScheduleJobLogger();
    }
}
