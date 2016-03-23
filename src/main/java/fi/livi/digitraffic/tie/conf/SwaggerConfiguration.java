package fi.livi.digitraffic.tie.conf;

import static com.google.common.base.Predicates.or;
import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_V1_BASE_PATH;
import static springfox.documentation.builders.PathSelectors.regex;

import java.util.Arrays;
import java.util.HashSet;

import com.google.common.base.Predicate;
import fi.livi.digitraffic.tie.service.MetadataApiInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    @Autowired
    MetadataApiInfoService metadataApiInfoService;

    @Bean
    public Docket metadataApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("metadata-api")
                .produces(new HashSet<>(Arrays.asList(MediaType.APPLICATION_JSON_UTF8_VALUE)))
                .apiInfo(metadataApiInfoService.getApiInfo())
                .select()
                .paths(getMetadataApiPaths())
                .build();
    }

    /**
     * Declares api paths to document by Swagger
     * @return api paths
     */
    private static Predicate<String> getMetadataApiPaths() {
        return or(
                regex(API_V1_BASE_PATH +"/*.*")
                //, regex(API_V2_PATH +"/*.*")
        );
    }
}