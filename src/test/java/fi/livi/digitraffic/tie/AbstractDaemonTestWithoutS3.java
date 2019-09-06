package fi.livi.digitraffic.tie;

import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = { "spring.localstack.enabled=false" })
public abstract class AbstractDaemonTestWithoutS3 extends AbstractTest {

}
