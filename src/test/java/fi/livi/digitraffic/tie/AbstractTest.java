package fi.livi.digitraffic.tie;

import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
    "config.test=true",
    "testcontainers.disabled=true",
    "dt.scheduled.annotation.enabled=false",
    "dt.job.scheduler.enabled=false",
    "spring.quartz.auto-startup=false",
    "spring.cloud.config.enabled=false",
    "logging.level.org.springframework.test.context.transaction.TransactionContext=WARN",
    "logging.level.com.tngtech.archunit=INFO",
    "roadConditions.baseUrl=https://roadConditions/",
    "roadConditions.suid=suid",
    "roadConditions.user=user",
    "roadConditions.pass=pass"
})
public abstract class AbstractTest {

}