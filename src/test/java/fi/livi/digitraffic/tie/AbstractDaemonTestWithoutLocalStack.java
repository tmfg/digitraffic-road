package fi.livi.digitraffic.tie;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = { "testcontainers.disabled=true" })
public abstract class AbstractDaemonTestWithoutLocalStack extends AbstractDaemonTest {

    @Autowired
    protected EntityManager entityManager;
}
