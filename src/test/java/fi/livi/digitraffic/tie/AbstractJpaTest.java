package fi.livi.digitraffic.tie;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import javax.transaction.Transactional;

import org.apache.commons.io.FileUtils;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit4.SpringRunner;

import fi.livi.digitraffic.tie.conf.RoadApplicationConfiguration;

@DataJpaTest(properties = "spring.main.web-application-type=none")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(RoadApplicationConfiguration.class)
@RunWith(SpringRunner.class)
@Transactional
public abstract class AbstractJpaTest {
    @Autowired
    protected ResourceLoader resourceLoader;

    protected String readResourceContent(final String resourcePattern) throws IOException {
        final Resource datex2Resource = loadResource(resourcePattern);

        return FileUtils.readFileToString(datex2Resource.getFile(), UTF_8);
    }

    protected Resource loadResource(final String pattern) {
        return resourceLoader.getResource(pattern);
    }
}
