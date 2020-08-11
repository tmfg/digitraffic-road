package fi.livi.digitraffic.tie;

import javax.transaction.Transactional;

import org.junit.runner.RunWith;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.AopTestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RoadApplication.class,
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = { "config.test=true", "logging.level.org.springframework.test.context.transaction.TransactionContext=WARN" })
@Transactional
public abstract class AbstractSpringJUnitTest extends AbstractTest {

    /**
     * Calls {@linkplain AopTestUtils#getTargetObject(Object)} }
     *
     * Get the <em>target</em> object of the supplied {@code candidate} object.
     * <p>If the supplied {@code candidate} is a Spring
     * {@linkplain AopUtils#isAopProxy proxy}, the target of the proxy will
     * be returned; otherwise, the {@code candidate} will be returned
     * <em>as is</em>.
     * @param candidate the instance to check (potentially a Spring AOP proxy;
     * never {@code null})
     * @return the target object or the {@code candidate} (never {@code null})
     * @throws IllegalStateException if an error occurs while unwrapping a proxy
     */
    public static <T> T getTargetObject(Object candidate) {
        return AopTestUtils.getTargetObject(candidate);
    }
}
