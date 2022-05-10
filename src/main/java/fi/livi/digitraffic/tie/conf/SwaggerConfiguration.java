package fi.livi.digitraffic.tie.conf;

import static springfox.documentation.builders.PathSelectors.regex;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

import fi.livi.digitraffic.tie.controller.ApiConstants;
import fi.livi.digitraffic.tie.controller.DtMediaType;
import fi.livi.digitraffic.tie.controller.v1.DataController;
import fi.livi.digitraffic.tie.controller.v1.MetadataController;
import fi.livi.digitraffic.tie.service.RoadApiInfoGetter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.plugins.WebFluxRequestHandlerProvider;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;
import springfox.documentation.swagger.web.DocExpansion;
import springfox.documentation.swagger.web.ModelRendering;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;

@ConditionalOnWebApplication
@Configuration
@ComponentScan(basePackageClasses = {
    DataController.class, MetadataController.class
})
public class SwaggerConfiguration {

    private final RoadApiInfoGetter roadApiInfoGetter;
    private final String host;
    private final String scheme;

    @Autowired
    public SwaggerConfiguration(final RoadApiInfoGetter roadApiInfoGetter,
                                final @Value("${dt.domain.url}") String domainUrl) throws URISyntaxException {
        this.roadApiInfoGetter = roadApiInfoGetter;
        URI uri = new URI(domainUrl);

        final int port = uri.getPort();
        if (port > -1) {
            host = uri.getHost() + ":" + port;
        } else {
            host = uri.getHost();
        }
        scheme = uri.getScheme();
    }

    @Bean
    public Docket roadApi() {
        return getDocket("road-api", getProductionApiPaths());
    }

    @Bean
    public Docket betaApi() {
        return getDocket("road-api-beta", getBetaApiPaths());
    }

    @Bean
    UiConfiguration uiConfiguration() {
        return UiConfigurationBuilder.builder()
            .docExpansion(DocExpansion.NONE)
            .defaultModelRendering(ModelRendering.MODEL)
            // There is bugs in online validator, so not use it at the moment ie. https://github.com/swagger-api/validator-badge/issues/97
            //.validatorUrl("https://online.swagger.io/validator")
            .build();
    }

    private Docket getDocket(final String groupName, final Predicate<String> apiPaths) {
        return new Docket(DocumentationType.SWAGGER_2)
            .host(host)
            .protocols(Set.of(scheme))
            .groupName(groupName)
            .produces(new HashSet<>(Collections.singletonList(DtMediaType.APPLICATION_JSON_VALUE)))
            .apiInfo(roadApiInfoGetter.getApiInfo())
            .select()
            .paths(apiPaths)
            .build()
            .useDefaultResponseMessages(false);
    }

    /**
     * Declares api paths to document by Swagger
     * @return api paths
     */
    private static Predicate<String> getProductionApiPaths() {
        // All starting with API but not containing BETA
        return regex("^(" + ApiConstants.API + ")+((?!" + ApiConstants.BETA + ").)*$");
    }

    private static Predicate<String> getBetaApiPaths() {
        // All containing BETA
        return regex(".*" + ApiConstants.BETA + ".*");
    }

    // DPO-1792 fix, TODO: remove when getting rid of springfox
    @Bean
    public static BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
        return new BeanPostProcessor() {

            @Override
            public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
                if (bean instanceof WebMvcRequestHandlerProvider || bean instanceof WebFluxRequestHandlerProvider) {
                    customizeSpringfoxHandlerMappings(getHandlerMappings(bean));
                }
                return bean;
            }

            private <T extends RequestMappingInfoHandlerMapping> void customizeSpringfoxHandlerMappings(final List<T> mappings) {
                final List<T> copy = mappings.stream()
                    .filter(mapping -> mapping.getPatternParser() == null)
                    .collect(Collectors.toList());
                mappings.clear();
                mappings.addAll(copy);
            }

            @SuppressWarnings("unchecked")
            private List<RequestMappingInfoHandlerMapping> getHandlerMappings(final Object bean) {
                try {
                    final Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");

                    if (field != null) {
                        field.setAccessible(true);
                        return (List<RequestMappingInfoHandlerMapping>) field.get(bean);
                    }

                    throw new IllegalStateException("no handlerMappings found");
                } catch (final IllegalArgumentException | IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }
}
