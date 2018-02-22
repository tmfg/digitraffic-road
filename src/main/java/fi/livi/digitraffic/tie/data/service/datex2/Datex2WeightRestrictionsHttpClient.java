package fi.livi.digitraffic.tie.data.service.datex2;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class Datex2WeightRestrictionsHttpClient {
    private static final Logger log = LoggerFactory.getLogger(Datex2WeightRestrictionsHttpClient.class);

    private final String url;
    private final RestTemplate restTemplate;
    private final RetryTemplate retryTemplate;

    @Autowired
    public Datex2WeightRestrictionsHttpClient(@Value("${datex2.weight.restrictions.url}") final String url,
        final RestTemplate restTemplate, final RetryTemplate retryTemplate) {
        this.url = url;
        this.restTemplate = restTemplate;
        this.retryTemplate = retryTemplate;
    }

    public String getWeightRestrictionsMessage() {
        log.info("Read datex2 weight limitations message");

        final StopWatch sw = StopWatch.createStarted();

        try {
            return retryTemplate.execute(context -> restTemplate.getForObject(url, String.class));
        } finally {
            log.info("Datex2 limitations tookMs={}", sw.getTime());
        }
    }

}
