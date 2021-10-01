package fi.livi.digitraffic.tie;

import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
    "config.test=true",
    "testcontainers.disabled=true",
    "spring.cloud.config.enabled=false",
    "logging.level.org.springframework.test.context.transaction.TransactionContext=WARN",
    "logging.level.com.tngtech.archunit=INFO"
})
public abstract class AbstractTest {

}