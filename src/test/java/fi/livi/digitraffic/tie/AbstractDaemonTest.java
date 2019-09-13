package fi.livi.digitraffic.tie;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = RoadApplication.class,
                properties = { "config.test=true", "app.type=daemon",
                               "spring.main.web-application-type=none" })
@TestPropertySource(properties = {
    "road.datasource.hikari.maximum-pool-size=2",
})
public abstract class AbstractDaemonTest extends AbstractTest {

}
