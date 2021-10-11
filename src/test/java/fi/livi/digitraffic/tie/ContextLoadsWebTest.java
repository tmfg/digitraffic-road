package fi.livi.digitraffic.tie;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class ContextLoadsWebTest extends AbstractSpringJUnitTest {
    @Test
    public void contextLoads() {
        assertNotNull(entityManager);
    }
}
