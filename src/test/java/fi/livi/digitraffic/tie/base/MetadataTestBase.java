package fi.livi.digitraffic.tie.base;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import fi.livi.digitraffic.tie.MetadataApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MetadataApplication.class,
                properties = {"config.test=true"})
@WebAppConfiguration
public abstract class MetadataTestBase {
}
