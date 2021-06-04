package fi.livi.digitraffic.tie;

import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = { "testcontainers.disabled=true" })
public class ContextLoadsWebTest extends AbstractSpringJUnitTest {
    @Test
    public void contextLoads() {
        assertNotNull(entityManager);
    }
}
