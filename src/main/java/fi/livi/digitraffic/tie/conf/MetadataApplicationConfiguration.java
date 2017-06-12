package fi.livi.digitraffic.tie.conf;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.Assert;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import fi.livi.digitraffic.tie.conf.jaxb2.Jaxb2TrafficDisordersDatex2ResponseHttpMessageConverter;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

@Configuration
@EnableJpaRepositories(basePackages = {"fi.livi.digitraffic.tie.metadata.dao", "fi.livi.digitraffic.tie.data.dao"})
@EnableTransactionManagement
@EnableRetry
public class MetadataApplicationConfiguration extends WebMvcConfigurerAdapter {

    public static final String API_V1_BASE_PATH = "/api/v1";
    public static final String API_METADATA_PART_PATH = "/metadata";
    public static final String API_DATA_PART_PATH = "/data";
    public static final String API_PLAIN_WEBSOCKETS_PART_PATH = "/plain-websockets";

    public static final String RETRY_OPERATION = "operation";

    private final ConfigurableApplicationContext applicationContext;

    @Autowired
    public MetadataApplicationConfiguration(final ConfigurableApplicationContext applicationContext) {
        Assert.notNull(applicationContext);
        this.applicationContext = applicationContext;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new Jaxb2TrafficDisordersDatex2ResponseHttpMessageConverter());
        super.configureMessageConverters(converters);
    }

    /**
     * Initialize OracleDataSource manually because datasource property spring.datasource.type=oracle.jdbc.pool.OracleDataSource
     * is not correctly handled by spring https://github.com/spring-projects/spring-boot/issues/6027#issuecomment-221582708
     * @param properties
     * @return
     * @throws SQLException
     */
    @Bean
    public DataSource dataSource(final DataSourceProperties properties) throws SQLException {
        final PoolDataSource dataSource = PoolDataSourceFactory.getPoolDataSource();
        dataSource.setUser(properties.getUsername());
        dataSource.setPassword(properties.getPassword());
        dataSource.setURL(properties.getUrl());
        dataSource.setFastConnectionFailoverEnabled(true);
        dataSource.setInitialPoolSize(5);
        dataSource.setMaxPoolSize(20);
        dataSource.setMinPoolSize(5);
        dataSource.setMaxIdleTime(5);
        dataSource.setValidateConnectionOnBorrow(true);
        dataSource.setMaxStatements(10);
        dataSource.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        /* Times are in seconds */
        /* The maximum connection reuse count allows connections to be gracefully closed and removed
           from the connection pool after a connection has been borrowed a specific number of times. */
        dataSource.setMaxConnectionReuseCount(100);
        /* The abandoned connection timeout enables borrowed connections to be reclaimed back into the connection pool after a connection
           has not been used for a specific amount of time. Abandonment is determined by monitoring calls to the database. */
        dataSource.setAbandonedConnectionTimeout(60);
        /* The time-to-live connection timeout enables borrowed connections to remain borrowed for a specific amount of time before the
           connection is reclaimed by the pool. This timeout feature helps maximize connection reuse and helps conserve systems resources
           that are otherwise lost on maintaining connections longer than their expected usage. */
        dataSource.setTimeToLiveConnectionTimeout(600);
        /* The inactive connection timeout specifies how long an available connection can remain idle before it is closed and removed from the pool.
           This timeout property is only applicable to available connections and does not affect borrowed connections. This property helps conserve
           resources that are otherwise lost on maintaining connections that are no longer being used. The inactive connection timeout
           (together with the maximum pool size) allows a connection pool to grow and shrink as application load changes. */
        dataSource.setInactiveConnectionTimeout(60);
        return dataSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        final SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.US);
        return slr;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        final LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    /**
     * Enables bean validation for controller parameters
     * @return
     */
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        LocaleChangeInterceptor localeChangeInterceptor = applicationContext.getBean(LocaleChangeInterceptor.class);
        Assert.notNull(localeChangeInterceptor, "LocaleChangeInterceptor cannot be null");
        registry.addInterceptor(localeChangeInterceptor);
    }

    /**
     * This redirects requests from root to /swagger-ui.html.
     * After that nginx redirects /swagger-ui.html to /api/v1/metadata/documentation/swagger-ui.html
     * @param registry
     */
    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("redirect:swagger-ui.html");
    }

    @Bean
    public RetryTemplate retryTemplate() {
        final SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(5);

        final FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(200); // 0.5 seconds

        final RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(retryPolicy);
        template.setBackOffPolicy(backOffPolicy);
        return template;
    }
}
