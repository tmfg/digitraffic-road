package fi.livi.digitraffic.tie.conf;

import java.nio.charset.Charset;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        final RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());

        // DPO-294 aineistot.vally.local palvelee UTF-8 merkistöllisiä xml-tiedostoja ilman encoding tietoa.
        // W3C:n ja RestTemplaten default on ISO-8859-1.
        // - https://www.w3.org/International/articles/http-charset/index
        // Tässä UTF-8 työnnetään sen edelle, jotta aineistot tarjoama vastaus tulee käsiteltyä oikein.
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        return restTemplate;
    }

    private static ClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(30 * 1000);
        factory.setReadTimeout(60 * 1000);
        return factory;
    }
}