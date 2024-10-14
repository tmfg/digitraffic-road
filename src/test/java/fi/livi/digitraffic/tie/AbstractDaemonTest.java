package fi.livi.digitraffic.tie;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
    "app.type=daemon",
    "spring.main.web-application-type=none",
    "logging.level.fi.livi.digitraffic.tie.service.weather.forecast.ForecastSectionV1MetadataUpdater=WARN",
    "camera-image-uploader.imageUpdateTimeout=500"
})
@SpringBootTest(classes = RoadApplication.class,
                webEnvironment = SpringBootTest.WebEnvironment.NONE)
public abstract class AbstractDaemonTest extends AbstractSpringJUnitTest {
}
