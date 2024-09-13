package fi.livi.digitraffic.tie;

import java.time.Instant;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.AopTestUtils;

import fi.livi.digitraffic.common.util.TimeUtil;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

/**
 To keep created context count as low as possible take in account that configuration
 parameters below affects used context cache key for the context caching.

 - locations (from @ContextConfiguration)
 - classes (part of @ContextConfiguration)
 - contextInitializerClasses (from @ContextConfiguration)
 - contextCustomizers (from ContextCustomizerFactory) – e.g. @DynamicPropertySource, @MockBean and @SpyBean.
 - contextLoader (part of @ContextConfiguration)
 - parent (from @ContextHierarchy)
 - activeProfiles (coming from @ActiveProfiles)
 - propertySourceLocations (from @TestPropertySource)
 - propertySourceProperties (from @TestPropertySource)
 - resourceBasePath (part of @WebAppConfiguration)

 @see <a href="https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#testcontext-ctx-management-caching">Spring Context Caching</a>
 */

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = RoadApplication.class,
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public abstract class AbstractSpringJUnitTest extends AbstractTest {

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected ConfigurableListableBeanFactory beanFactory;

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
    public static <T> T getTargetObject(final Object candidate) {
        return AopTestUtils.getTargetObject(candidate);
    }

    protected boolean isBeanRegistered(final Class<?> c) {
        try {
            return beanFactory.getBean(c) != null;
        } catch (final NoSuchBeanDefinitionException e) {
            return false;
        }
    }
    public Instant getTransactionTimestamp() {
        return (Instant)entityManager.createNativeQuery("select now()").getSingleResult();
    }

    public Instant getTransactionTimestampRoundedToSeconds() {
        TimeUtil.roundInstantSeconds(getTransactionTimestamp());
        return TimeUtil.roundInstantSeconds(getTransactionTimestamp());
    }
}
