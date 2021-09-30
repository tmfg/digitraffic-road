package fi.livi.digitraffic.tie;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class ContextLoadsDaemonTest extends AbstractDaemonTest {

    @Test
    public void contextLoads() {
        assertNotNull(entityManager);
    }
}
