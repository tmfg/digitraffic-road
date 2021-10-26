package fi.livi.digitraffic.tie;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
    "app.type=daemon",
    "spring.main.web-application-type=none",
    "road.datasource.hikari.maximum-pool-size=2",
    "logging.level.fi.livi.digitraffic.tie.service.v1.forecastsection.ForecastSectionV1MetadataUpdater=WARN",
    "logging.level.fi.livi.digitraffic.tie.service.v1.camera.CameraImageUpdateService=WARN",
    "camera-image-uploader.imageUpdateTimeout=500"
})
@SpringBootTest(classes = RoadApplication.class,
                webEnvironment = SpringBootTest.WebEnvironment.NONE)
public abstract class AbstractDaemonTest extends AbstractSpringJUnitTest {

}
