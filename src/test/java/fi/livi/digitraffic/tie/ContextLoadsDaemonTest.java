package fi.livi.digitraffic.tie;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(classes = RoadApplication.class)
public class ContextLoadsDaemonTest extends AbstractDaemonTestWithoutS3 {

    @Test
    public void contextLoads() {
    }

}
