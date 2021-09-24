package fi.livi.digitraffic.tie;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
    "config.test=true",
    "testcontainers.disabled=true",
    "spring.cloud.config.enabled=false",
    "logging.level.org.springframework.test.context.transaction.TransactionContext=WARN",
    "logging.level.com.tngtech.archunit=INFO"
})
public abstract class AbstractTest {

    private static final Logger log = LoggerFactory.getLogger(AbstractTest.class);
    protected static final ResourceLoader resourceLoader = new DefaultResourceLoader();

    @Autowired
    protected JdbcTemplate jdbcTemplate;
    @PersistenceContext
    protected EntityManager entityManager;
    @Autowired
    protected GenericApplicationContext applicationContext;
}
