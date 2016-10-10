package fi.livi.digitraffic.tie;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MetadataApplication.class,
                webEnvironment = SpringBootTest.WebEnvironment.NONE,
                properties = {"spring.main.web_environment=false"})
public abstract class AbstractIntegrationMetadataTest {
}
