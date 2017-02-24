package fi.livi.digitraffic.tie.base;

import org.springframework.boot.test.context.SpringBootTest;

import fi.livi.digitraffic.tie.MetadataApplication;

@SpringBootTest(classes = MetadataApplication.class,
                webEnvironment = SpringBootTest.WebEnvironment.NONE,
                properties = {"config.test=true",
                              "spring.main.web_environment=false"})
public abstract class AbstractMetadataIntegrationTest extends AbstractMetadataWebTestBase {

}
