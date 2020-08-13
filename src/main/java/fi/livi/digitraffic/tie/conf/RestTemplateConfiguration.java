package fi.livi.digitraffic.tie.conf;

import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {

    private static final int DEFAULT_CONNECT_TIMEOUT_S = 30;
    private static final int DEFAULT_READ_TIMEOUT_S = 60;

    @Bean
    public RestTemplate restTemplate() {
        return createRestTemplate(30, 60);
    }

    public static RestTemplate createRestTemplate(final int connectTimeoutSeconds, int readTimeoutSeconds) {
        final RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory(DEFAULT_CONNECT_TIMEOUT_S, DEFAULT_READ_TIMEOUT_S));

        // DPO-294 aineistot.vally.local palvelee UTF-8 merkistöllisiä xml-tiedostoja ilman encoding tietoa.
        // W3C:n ja RestTemplaten default on ISO-8859-1.
        // - https://www.w3.org/International/articles/http-charset/index
        // Tässä UTF-8 työnnetään sen edelle, jotta aineistot tarjoama vastaus tulee käsiteltyä oikein.
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }

    private static ClientHttpRequestFactory clientHttpRequestFactory(final int connectTimeoutSeconds, int readTimeoutSeconds) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutSeconds * 1000);
        factory.setReadTimeout(readTimeoutSeconds * 1000);
        return factory;
    }
}