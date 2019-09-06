package fi.livi.digitraffic.tie;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = RoadApplication.class)
@TestPropertySource(properties = { "app.type=daemon", "spring.main.web-application-type=none" })
public abstract class AbstractDaemonTest extends AbstractTest {

}
