package fi.livi.digitraffic.tie.conf;

import static org.slf4j.LoggerFactory.getLogger;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import javax.servlet.Filter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RegExUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.TransformedResource;

import fi.livi.digitraffic.tie.conf.jaxb2.Jaxb2D2LogicalModelHttpMessageConverter;
import fi.livi.digitraffic.tie.conf.jaxb2.Jaxb2Datex2ResponseHttpMessageConverter;
import fi.livi.digitraffic.tie.controller.DtMediaType;

@ConditionalOnWebApplication
@Configuration
public class RoadWebApplicationConfiguration implements WebMvcConfigurer {
    private static final Logger log = getLogger(RoadWebApplicationConfiguration.class);

    // Match when there is no http in location start
    private final static String SCHEMA_LOCATION_REGEXP = "schemaLocation=\"((?!http))";
    private final static String SCHEMA_PATH = "/schemas/datex2/";
    private final String schemaDomainUrlAndPath;

    @Autowired
    public RoadWebApplicationConfiguration(final ConfigurableApplicationContext applicationContext,
                                           final @Value("${dt.domain.url}") String schemaDomainUrl) {
        this.schemaDomainUrlAndPath = schemaDomainUrl + SCHEMA_PATH;
    }

    /** Support for etag and conditional HTTP-requests */
    @ConditionalOnProperty(value = "etags.enabled", havingValue = "true")
    @Bean
    public Filter ShallowEtagHeaderFilter() {
        final ShallowEtagHeaderFilter shallowEtagHeaderFilter = new ShallowEtagHeaderFilter();
        shallowEtagHeaderFilter.setWriteWeakETag(false);
        return shallowEtagHeaderFilter;
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // put first
        converters.add(0, new Jaxb2D2LogicalModelHttpMessageConverter(schemaDomainUrlAndPath));
        converters.add(0, new Jaxb2Datex2ResponseHttpMessageConverter(schemaDomainUrlAndPath));
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Add datex2 schema locations to default static file path and
        registry.addResourceHandler(SCHEMA_PATH + "/**")
            .addResourceLocations("classpath:/schemas/datex2/")
            // cache resource -> this is converted only once per resource
            .resourceChain(true)
            // add current domain and path to schemaLocation-attribute
            .addTransformer((request, resource, transformerChain) -> {
                final String schema = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
                final String newSchema =
                    RegExUtils.replaceAll(schema, SCHEMA_LOCATION_REGEXP, String.format("schemaLocation=\"%s", schemaDomainUrlAndPath));
                return new TransformedResource(resource, newSchema.getBytes());
            });
    }

    /**
     * Enables bean validation for controller parameters
     * @return MethodValidationPostProcessor
     */
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new AllowedParameterInterceptor());
        registry.addInterceptor(new DeprecationInterceptor());
    }

    /**
     * This redirects requests from root / to /swagger-ui/index.html.
     * After that nginx redirects /swagger-ui.html to /api/v1/metadata/documentation/swagger-ui.html
     * @param registry current ViewControllerRegistry to modify
     */
    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("redirect:/swagger-ui/index.html");
    }

    @Override
    public void configureContentNegotiation(final ContentNegotiationConfigurer configurer) {
        configurer.strategies(List.of(new CustomHeaderContentNegotiationStrategy()));
        configurer.defaultContentType(DtMediaType.APPLICATION_JSON);
    }

    private static class CustomHeaderContentNegotiationStrategy implements ContentNegotiationStrategy {

        // Spring default implementation uses only HeaderContentNegotiationStrategy
        private final HeaderContentNegotiationStrategy headerStragegy = new HeaderContentNegotiationStrategy();

        @Override
        public List<MediaType> resolveMediaTypes(final NativeWebRequest webRequest) throws HttpMediaTypeNotAcceptableException {
            final List<MediaType> fromHeaders = headerStragegy.resolveMediaTypes(webRequest);
            try {
                // By default many client's sends long list of accepted types or */* etc.
                // If specific path is asked, then check if json is in accepted formats return json otherwise xml.
                final String path = ((ServletWebRequest) webRequest).getRequest().getRequestURI();
                if ( path.contains("datex2") ) {
                    return containsJson(fromHeaders) ?
                                Collections.singletonList(DtMediaType.APPLICATION_JSON) :
                                Collections.singletonList(DtMediaType.APPLICATION_XML);
                }
            } catch (final Error e) {
                log.error("method=resolveMediaTypes", e);
            }
            // Returns default implementation value
            return fromHeaders;
        }

        boolean containsJson(final List<MediaType> mediaTypes ) {
            return mediaTypes.stream()
                .anyMatch(mediaType -> mediaType.getType().equals(DtMediaType.APPLICATION_JSON.getType()) &&
                                       mediaType.getSubtype().equals(DtMediaType.APPLICATION_JSON.getSubtype()));
        }
    }
}
