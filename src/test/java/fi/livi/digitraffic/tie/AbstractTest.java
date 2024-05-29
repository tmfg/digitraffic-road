package fi.livi.digitraffic.tie;

import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
    "config.test=true",
    "dt.scheduled.annotation.enabled=false",
    "dt.job.scheduler.enabled=false",
    "spring.quartz.auto-startup=false",
    "spring.cloud.config.enabled=false",
    "logging.level.org.springframework.test.context.transaction.TransactionContext=WARN",
    "logging.level.com.tngtech.archunit=INFO",
    "roadConditions.baseUrl=https://roadConditions/",
    "roadConditions.suid=suid",
    "roadConditions.user=user",
    "roadConditions.pass=pass",
    // https://github.com/spring-projects/spring-boot/issues/39735 as default Tomcat listens 0.0.0.0 and stopping takes ages
    "server.address=127.0.0.1"
})
public abstract class AbstractTest {

}