package fi.livi.digitraffic.tie.base;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fi.livi.digitraffic.tie.MetadataApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MetadataApplication.class,
                webEnvironment = SpringBootTest.WebEnvironment.NONE,
                properties = {"config.test=true",
                              "spring.main.web_environment=false"})
public abstract class MetadataIntegrationTest extends MetadataTestBase {

    @Before
    public void before() {
        generateMissingLotjuIds();
        fixData();
    }

    @After
    public void after() {
        restoreGeneratedLotjuIds();
    }
}
