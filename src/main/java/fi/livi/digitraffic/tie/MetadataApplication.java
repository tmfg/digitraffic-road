package fi.livi.digitraffic.tie;

import fi.livi.digitraffic.tie.service.MetadataApiInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories
@EnableSwagger2
public class MetadataApplication extends WebMvcConfigurerAdapter {

    @Autowired
    LocaleChangeInterceptor localeChangeInterceptor;

    @Autowired
    MetadataApiInfoService metadataApiInfoService;

    public static void main(String[] args) {
        SpringApplication.run(MetadataApplication.class, args);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor);
    }
}
