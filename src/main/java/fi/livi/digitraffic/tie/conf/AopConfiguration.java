package fi.livi.digitraffic.tie.conf;

import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import fi.livi.digitraffic.tie.aop.PerformanceMonitorAspect;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Aspect
public class AopConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AopConfiguration.class);
    private PerformanceMonitorAspect performanceMonitorAspect;

    @Bean
    public PerformanceMonitorAspect performanceMonitorAspect() {
        this.performanceMonitorAspect = new PerformanceMonitorAspect();
        return performanceMonitorAspect;
    }
}
