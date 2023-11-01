package fi.livi.digitraffic.tie.service;

import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import fi.livi.digitraffic.tie.annotation.NotTransactionalServiceMethod;

@ConditionalOnNotWebApplication
@Service
public class RestTemplateGzipService {
    private static final Logger log = LoggerFactory.getLogger(RestTemplateGzipService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public RestTemplateGzipService(final RestTemplate restTemplate, final ObjectMapper objectMapper) {

        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @NotTransactionalServiceMethod
    public  <T> T getForGzippedObject(final String url, final Class<T> returnType) {
        return getForGzippedObject(url, returnType, null, null);
    }

    @NotTransactionalServiceMethod
    public  <T> T getForGzippedObject(final String url, final Class<T> returnType, final String methodToLog, final String urlToLog) {
        final StopWatch timer = StopWatch.createStarted();
        return restTemplate.execute(
            url,
            HttpMethod.GET,
            (ClientHttpRequest requestCallback) -> requestCallback.getHeaders().setContentType(MediaType.APPLICATION_OCTET_STREAM),
            responseExtractor -> {
                try(final InputStream responseBodyIs = responseExtractor.getBody();
                    final GZIPInputStream gZipIs = new GZIPInputStream(responseBodyIs)) {
                    final ObjectReader reader = objectMapper.readerFor(returnType);
                    return reader.readValue(gZipIs, returnType);

                } catch (final Exception e) {
                    log.error("method=getForGzippedObject" + (methodToLog != null ?  " / " + methodToLog : "") + " failed", e);
                    throw e;
                } finally {
                    log.info("method={} for type {} tookMs={} url: {}",
                             methodToLog != null ? methodToLog : "getForGzippedObject",  returnType.getName(),
                             timer.getTime(), StringUtils.firstNonBlank(urlToLog, "hidden"));
                }
            });
    }
}
