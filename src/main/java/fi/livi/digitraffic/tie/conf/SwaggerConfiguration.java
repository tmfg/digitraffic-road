package fi.livi.digitraffic.tie.conf;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.SwaggerUiConfigProperties;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import fi.livi.digitraffic.tie.controller.ApiConstants;
import fi.livi.digitraffic.tie.controller.v1.DataController;
import fi.livi.digitraffic.tie.controller.v1.MetadataController;
import fi.livi.digitraffic.tie.documentation.RoadApiInfo;
import fi.livi.digitraffic.tie.service.RoadApiInfoGetter;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@ConditionalOnWebApplication
@Configuration
@ComponentScan(basePackageClasses = {
    DataController.class, MetadataController.class
})
public class SwaggerConfiguration {

    private final RoadApiInfoGetter roadApiInfoGetter;
    private final RoadApiInfo roadApiInfo;

    private final String API_PATHS = ApiConstants.API + "/**";
    private final String BETA_PATHS = "/**" + ApiConstants.BETA + "/**";
    private final String host;
    private final String scheme;

    @Autowired
    public SwaggerConfiguration(final RoadApiInfoGetter roadApiInfoGetter,
                                final @Value("${dt.domain.url}") String domainUrl) throws URISyntaxException {
        this.roadApiInfoGetter = roadApiInfoGetter;
        this.roadApiInfo = roadApiInfoGetter.getApiInfo();

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
    public GroupedOpenApi roadApi() {
        return GroupedOpenApi.builder()
            .group("road-api")
            .pathsToMatch(API_PATHS)
            .pathsToExclude(BETA_PATHS)
            .addOpenApiCustomiser(openApiConfig())
            .build();
    }

    @Bean
    public GroupedOpenApi roadApiBeta() {
        return GroupedOpenApi.builder()
            .group("road-api-beta")
            .pathsToMatch(BETA_PATHS)
            .addOpenApiCustomiser(openApiConfig())
            .build();
    }

    @Bean
    public SwaggerUiConfigProperties swaggerUiConfig() {
        SwaggerUiConfigProperties config = new SwaggerUiConfigProperties();
        config.setDocExpansion("none");
        config.setDefaultModelRendering("example");
        config.setTagsSorter("alpha");
        return config;
    }

    private OpenApiCustomiser openApiConfig() {
        return openApi -> {
            openApi
                .setInfo(new Info()
                    .title(roadApiInfo.getTitle())
                    .description(roadApiInfo.getDescription())
                    .version(roadApiInfo.getVersion())
                    .contact(roadApiInfo.getContact())
                    .termsOfService(roadApiInfo.getTermsOfServiceUrl())
                    .license(roadApiInfo.getLicense()));

            final Server server = new Server();
            final String url = scheme + "://" + host;
            server.setUrl(url);

            openApi.setServers(Arrays.asList(server));
        };
    }

}
