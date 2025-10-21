package fi.livi.digitraffic.tie.conf.datex2;

import fi.livi.digitraffic.tie.conf.jaxb2.DatexII_3_NamespacePrefixMapper;
import fi.livi.digitraffic.tie.conf.jaxb2.Jaxb2RootElementHttpMessageConverter;
import fi.livi.digitraffic.tie.datex2.v3_5.SituationPublication;
import fi.livi.digitraffic.tie.datex2.v3_5.PayloadPublication;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;

@ConditionalOnWebApplication
@Configuration
public class Datex2_3_5_Configuration {
    @Bean
    public HttpMessageConverter<Object> xmlHttpMessageConverterForD2SituationPublication() {
        return new Jaxb2RootElementHttpMessageConverter<>(
                    SituationPublication.class, PayloadPublication.class, "payload")
            .withJaxbSchemaLocations("https://datex2.eu/schema/3/situation")
//            .withNamespacePrefixMapper(new DatexII_3_NamespacePrefixMapper())
            .withNamespaceURI("http://datex2.eu/schema/3/situation");
    }

}
