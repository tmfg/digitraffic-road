package fi.livi.digitraffic.tie;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = RoadApplication.class,
                properties = { "config.test=true", "app.type=daemon",
                               "spring.main.web-application-type=none" })
public class ContextLoadsDaemonTest extends AbstractTest {

    @Test
    public void contextLoads() {
    }

}
