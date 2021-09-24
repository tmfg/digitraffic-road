package fi.livi.digitraffic.tie;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ContextLoadsDaemonTest extends AbstractDaemonTest {

    @Test
    public void contextLoads() {
        assertNotNull(entityManager);
    }
}
