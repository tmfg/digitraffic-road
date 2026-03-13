package fi.livi.digitraffic.tie;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jsonb.autoconfigure.JsonbAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.TestDatabaseAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import fi.livi.digitraffic.tie.conf.RoadApplicationConfiguration;
import fi.livi.digitraffic.tie.conf.TestCacheManagerConfiguration;

@DataJpaTest(properties = {"spring.main.web-application-type=none"},
             excludeAutoConfiguration = {
             TestDatabaseAutoConfiguration.class, DataSourceAutoConfiguration.class},
             showSql = false)
@Import({RoadApplicationConfiguration.class, JsonbAutoConfiguration.class, JacksonAutoConfiguration.class,
         TestCacheManagerConfiguration.class})
@ExtendWith(SpringExtension.class)
@Transactional
public abstract class AbstractJpaTest extends AbstractTest {

    @PersistenceContext
    protected EntityManager entityManager;
}
