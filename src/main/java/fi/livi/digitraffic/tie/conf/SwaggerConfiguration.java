package fi.livi.digitraffic.tie.conf;

import static com.google.common.base.Predicates.or;
import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_V1_BASE_PATH;
import static springfox.documentation.builders.PathSelectors.regex;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;

import com.google.common.base.Predicate;

import fi.livi.digitraffic.tie.metadata.service.MetadataApiInfoService;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@ConditionalOnProperty(name = "spring.main.web_environment", havingValue = "true", matchIfMissing = true)
@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    private final MetadataApiInfoService metadataApiInfoService;

    @Autowired
    public SwaggerConfiguration(final MetadataApiInfoService metadataApiInfoService) {
        Assert.notNull(metadataApiInfoService);
        this.metadataApiInfoService = metadataApiInfoService;
    }

    @Bean
    public Docket metadataApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("metadata-api")
                .directModelSubstitute(ZonedDateTime.class, String.class)
                .directModelSubstitute(LocalDateTime.class, String.class)
                .directModelSubstitute(LocalDate.class, String.class)
                .directModelSubstitute(Date.class, String.class)
                .produces(new HashSet<>(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8_VALUE)))
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
