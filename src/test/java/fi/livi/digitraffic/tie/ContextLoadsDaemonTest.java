package fi.livi.digitraffic.tie;

import org.junit.jupiter.api.Test;import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ContextLoadsDaemonTest extends AbstractDaemonTestWithoutLocalStack {

    @Test
    public void contextLoads() {
        assertNotNull(entityManager);
    }
}
