package fi.livi.digitraffic.tie;

import org.junit.jupiter.api.Test;import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ContextLoadsDaemonTest extends AbstractDaemonTestWithouLocalStack {

    @Test
    public void contextLoads() {
    }

}
