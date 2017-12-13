package fi.livi.digitraffic.tie.conf;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.Assert;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import fi.livi.digitraffic.tie.conf.jaxb2.Jaxb2Datex2ResponseHttpMessageConverter;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

@Configuration
@EnableJpaRepositories(basePackages = {"fi.livi.digitraffic.tie.metadata.dao", "fi.livi.digitraffic.tie.data.dao"})
@EnableTransactionManagement
@EnableRetry
public class MetadataApplicationConfiguration extends WebMvcConfigurerAdapter {

    public static final String API_V1_BASE_PATH = "/api/v1";
    public static final String API_BETA_BASE_PATH = "/api/beta";
    public static final String API_METADATA_PART_PATH = "/metadata";
    public static final String API_DATA_PART_PATH = "/data";
    public static final String API_PLAIN_WEBSOCKETS_PART_PATH = "/plain-websockets";

    private final ConfigurableApplicationContext applicationContext;

    @Value("${ci.datasource.initialPoolSize:1}")
    private Integer INITIAL_POOL_SIZE;
    @Value("${ci.datasource.minPoolSize:1}")
    private Integer MIN_POOL_SIZE;
    @Value("${ci.datasource.maxPoolSize:20}")
    private Integer MAX_POOL_SIZE;

    @Autowired
    public MetadataApplicationConfiguration(final ConfigurableApplicationContext applicationContext) {
        Assert.notNull(applicationContext);
        this.applicationContext = applicationContext;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new Jaxb2Datex2ResponseHttpMessageConverter());
        super.configureMessageConverters(converters);
    }

    /**
     * Initialize OracleDataSource manually because datasource property spring.datasource.type=oracle.jdbc.pool.OracleDataSource
     * is not correctly handled by spring https://github.com/spring-projects/spring-boot/issues/6027#issuecomment-221582708
     * @param properties
     * @return
     * @throws SQLException
     */
    @SuppressWarnings("Duplicates")
    @Bean
    public DataSource dataSource(final DataSourceProperties properties) throws SQLException {
        final PoolDataSource dataSource = PoolDataSourceFactory.getPoolDataSource();
        dataSource.setUser(properties.getUsername());
        dataSource.setPassword(properties.getPassword());
        dataSource.setURL(properties.getUrl());
        dataSource.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        final String DPO_365_SOCKET_READ_TIMEOUT_FIX = "120000";
        dataSource.setConnectionProperty("oracle.jdbc.ReadTimeout", DPO_365_SOCKET_READ_TIMEOUT_FIX);
        dataSource.setFastConnectionFailoverEnabled(true);
        dataSource.setValidateConnectionOnBorrow(true);
        dataSource.setSQLForValidateConnection("select 1 from dual"); // DPO-365 try to fix "IO Error: Socket closed" errors.
        dataSource.setInitialPoolSize(INITIAL_POOL_SIZE);
        dataSource.setMaxPoolSize(MAX_POOL_SIZE);
        dataSource.setMinPoolSize(MIN_POOL_SIZE);
        dataSource.setMaxStatements(25);
        dataSource.setAbandonedConnectionTimeout(60);
        dataSource.setTimeToLiveConnectionTimeout(480);
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
}
