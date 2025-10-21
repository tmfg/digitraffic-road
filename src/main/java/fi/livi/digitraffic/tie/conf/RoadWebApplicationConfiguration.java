package fi.livi.digitraffic.tie.conf;

import static org.slf4j.LoggerFactory.getLogger;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.lang.NonNull;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.TransformedResource;

import fi.livi.digitraffic.common.config.DeprecationInterceptor;
import fi.livi.digitraffic.common.config.resolvers.NullFilteringVarargsResolver;
import fi.livi.digitraffic.common.util.StringUtil;
import fi.livi.digitraffic.tie.conf.jaxb2.DatexII_3_NamespacePrefixMapper;
import fi.livi.digitraffic.tie.conf.jaxb2.Jaxb2RootElementHttpMessageConverter;
import fi.livi.digitraffic.tie.controller.DtMediaType;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.D2LogicalModel;
import fi.livi.digitraffic.tie.tms.datex2.v3_5.MeasuredDataPublication;
import fi.livi.digitraffic.tie.tms.datex2.v3_5.MeasurementSiteTablePublication;
import fi.livi.digitraffic.tie.tms.datex2.v3_5.PayloadPublication;
import jakarta.servlet.Filter;

@ConditionalOnWebApplication
@Configuration
public class RoadWebApplicationConfiguration implements WebMvcConfigurer {
    private static final Logger log = getLogger(RoadWebApplicationConfiguration.class);

    // Match when there is no http in location start
    private final static String SCHEMA_LOCATION_REGEXP = "schemaLocation=\"((?!http))";
    private final static String DATEX2_SCHEMA_ROOT_PATH = "/schemas/datex2";
    private final String dtDomainAndSchemaRootLocation;
    private final String dtDomain;

    @Autowired
    public RoadWebApplicationConfiguration(final ConfigurableApplicationContext applicationContext,
                                           final @Value("${dt.domain.url}") String domainUrl) {

        try {
            // For some reason @Value is not working on test
            final String schemaDomainUrl =
                    StringUtils.isNotBlank(domainUrl) && StringUtils.containsNone(domainUrl, "${") ?
                    domainUrl :
                    applicationContext.getEnvironment().getProperty("dt.domain.url");

            this.dtDomain =
                    Objects.requireNonNull(schemaDomainUrl).replaceAll("https", "http"); // Schema is always http
            this.dtDomainAndSchemaRootLocation =
                    new URI(StringUtil.format("{}/{}", dtDomain, DATEX2_SCHEMA_ROOT_PATH)).normalize().toString();
        } catch (final URISyntaxException e) {
            log.error("Failed to create dtDomainAndSchemaRootLocation {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Support for etag and conditional HTTP-requests
     */
    @ConditionalOnProperty(value = "etags.enabled",
                           havingValue = "true")
    @Bean
    public Filter ShallowEtagHeaderFilter() {
        final ShallowEtagHeaderFilter shallowEtagHeaderFilter = new ShallowEtagHeaderFilter();
        shallowEtagHeaderFilter.setWriteWeakETag(false);
        return shallowEtagHeaderFilter;
    }

    @Bean
    public HttpMessageConverter<Object> xmlHttpMessageConverterForD2LogicalModel() {
        return new Jaxb2RootElementHttpMessageConverter<>(D2LogicalModel.class)
                .withJaxbSchemaLocations(
                        "https://datex2.eu/schema/2/2_0",
                        dtDomainAndSchemaRootLocation + "/2_2_3_fi/DATEXIISchema_2_2_3_with_definitions_FI.xsd");
    }

    @Bean
    public HttpMessageConverter<Object> xmlHttpMessageConverterForMeasurementSiteTablePublication() {
        // To return child class in xml as xsi:type attribute we need to use custom implementation
        // telling the child and parent classes
        return new Jaxb2RootElementHttpMessageConverter<>(
                MeasurementSiteTablePublication.class,
                PayloadPublication.class,
                "payload")
            .withJaxbSchemaLocations("https://datex2.eu/schema/3/d2Payload")
            .withNamespacePrefixMapper(new DatexII_3_NamespacePrefixMapper())
            .withNamespaceURI("http://datex2.eu/schema/3/d2Payload");
    }

    @Bean
    public HttpMessageConverter<Object> xmlHttpMessageConverterForMeasuredDataPublication() {
        // To return child class in xml as xsi:type attribute we need to use custom implementation
        // telling the child and parent classes
        return new Jaxb2RootElementHttpMessageConverter<>(
                MeasuredDataPublication.class,
                PayloadPublication.class,
                "payload")
                .withJaxbSchemaLocations("https://datex2.eu/schema/3/d2Payload")
                .withNamespacePrefixMapper(new DatexII_3_NamespacePrefixMapper())
                .withNamespaceURI("http://datex2.eu/schema/3/d2Payload");
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        // Add datex2 schema locations to default static file path and
        registry.addResourceHandler(DATEX2_SCHEMA_ROOT_PATH + "/**")
                .addResourceLocations("classpath:/schemas/datex2/")
                // cache resource -> this is converted only once per resource
                .resourceChain(true)
                // add current domain and path to schemaLocation-attribute
                .addTransformer((request, resource, transformerChain) -> {
                    // Get path part: /foo/bar/schema.sxd -> /foo/bar
                    final String requestPath = StringUtils.substringBeforeLast(request.getRequestURI(), "/");
                    // Create new uri https://dtDomain/foo/bar/
                    final String domainAndPath =
                            URI.create(StringUtil.format("{}/{}/", dtDomain, requestPath)).normalize().toString();

                    final String schema = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
                    final String newSchema =
                            RegExUtils.replaceAll(schema, SCHEMA_LOCATION_REGEXP,
                                    StringUtil.format("schemaLocation=\"{}", domainAndPath));
                    return new TransformedResource(resource, newSchema.getBytes());
                })
        ;
    }

    /**
     * Enables bean validation for controller parameters
     *
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
     *
     * @param registry current ViewControllerRegistry to modify
     */
    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("redirect:/swagger-ui/index.html");
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new NullFilteringVarargsResolver());
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
        @NonNull
        public List<MediaType> resolveMediaTypes(
                @NonNull
                final NativeWebRequest webRequest) throws HttpMediaTypeNotAcceptableException {
            final List<MediaType> fromHeaders = headerStragegy.resolveMediaTypes(webRequest);
            try {
                // By default many client's sends long list of accepted types or */* etc.
                // If specific path is asked, then check if json is in accepted formats return json otherwise xml.
                final String path = ((ServletWebRequest) webRequest).getRequest().getRequestURI();
                if (StringUtils.endsWith(path, ".json")) {
                    return Collections.singletonList(DtMediaType.APPLICATION_JSON);
                } else if (StringUtils.endsWith(path, ".xml")) {
                    return Collections.singletonList(DtMediaType.APPLICATION_XML);
                } else if (StringUtils.contains(path, ".datex2")) {
                    if (containsJson(fromHeaders)) {
                        log.info("method=resolveMediaTypes type=json for path={} mediaTypes: {}", path, fromHeaders);
                    }
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

        boolean containsJson(final List<MediaType> mediaTypes) {
            return mediaTypes.stream()
                    .anyMatch(mediaType -> mediaType.getType().equals(DtMediaType.APPLICATION_JSON.getType()) &&
                            mediaType.getSubtype().equals(DtMediaType.APPLICATION_JSON.getSubtype()));
        }
    }
}
