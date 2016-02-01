package fi.livi.digitraffic.tie;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

import java.util.Locale;

import com.google.common.base.Predicate;
import fi.livi.digitraffic.tie.controller.AbstractMetadataController;
import fi.livi.digitraffic.tie.service.MetadataApiInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
@ComponentScan
public class MetadataApplicationConfiguration {

    @Autowired
    MetadataApiInfoService metadataApiInfoService;

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.US);
        return slr;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    @Bean
    public Docket metadataApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("metadata-api")
                .apiInfo(metadataApiInfoService.getApiInfo())
                .select()
                .paths(metadataApiPaths())
                .build();
    }

    private Predicate<String> metadataApiPaths() {
        return or(
                regex(AbstractMetadataController.API_V1_PATH +"/*.*")
        );
    }
}
