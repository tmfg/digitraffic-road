package fi.livi.digitraffic.tie;

import javax.transaction.Transactional;

import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import fi.livi.digitraffic.tie.conf.RoadApplicationConfiguration;

@DataJpaTest(properties = "spring.main.web-application-type=none")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(RoadApplicationConfiguration.class)
@RunWith(SpringRunner.class)
@Transactional
public abstract class AbstractJpaTest {
}
