package fi.livi.digitraffic.tie.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {
    @Bean
    public WebClient webClient() {
        // more memory for default web-client
        return WebClient.builder()
            .exchangeStrategies(ExchangeStrategies.builder()
                .codecs(codecs -> codecs
                    .defaultCodecs()
                    .maxInMemorySize(10 * 1024 * 1024))
                .build())
            .build();
    }
}
