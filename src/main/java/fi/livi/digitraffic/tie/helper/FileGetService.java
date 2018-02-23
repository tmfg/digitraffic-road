package fi.livi.digitraffic.tie.helper;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class FileGetService {
    private static final Logger log = LoggerFactory.getLogger(FileGetService.class);

    private final RestTemplate restTemplate;
    private final RetryTemplate retryTemplate;

    public FileGetService(final RestTemplate restTemplate, final RetryTemplate retryTemplate) {
        this.restTemplate = restTemplate;
        this.retryTemplate = retryTemplate;
    }

    public <T> T getFile(final String integration, final String url, final Class<T> clazz) {
        final StopWatch sw = StopWatch.createStarted();

        try {
            return retryTemplate.execute(context -> restTemplate.getForObject(url, clazz));
        } finally {
            log.info("url={} integration={} tookMs={} className={}", url, integration, sw.getTime(), clazz.getSimpleName());
        }
    }

}
