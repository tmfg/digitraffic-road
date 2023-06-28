package fi.livi.digitraffic.tie;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jsonb.JsonbAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import fi.livi.digitraffic.tie.conf.RoadApplicationConfiguration;

@DataJpaTest(properties = "spring.main.web-application-type=none", excludeAutoConfiguration = {FlywayAutoConfiguration.class,
             LiquibaseAutoConfiguration.class, TestDatabaseAutoConfiguration.class, DataSourceAutoConfiguration.class},
             showSql = false)
@Import({RoadApplicationConfiguration.class, JsonbAutoConfiguration.class, JacksonAutoConfiguration.class })
@ExtendWith(SpringExtension.class)
@Transactional
public abstract class AbstractJpaTest extends AbstractTest {

    @PersistenceContext
    protected EntityManager entityManager;
}
