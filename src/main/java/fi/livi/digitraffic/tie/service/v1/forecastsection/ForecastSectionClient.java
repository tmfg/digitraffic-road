package fi.livi.digitraffic.tie.service.v1.forecastsection;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import fi.livi.digitraffic.tie.service.v1.forecastsection.dto.v1.ForecastSectionCoordinatesDto;
import fi.livi.digitraffic.tie.service.v1.forecastsection.dto.v1.ForecastSectionCoordinatesEntry;
import fi.livi.digitraffic.tie.service.v1.forecastsection.dto.v2.ForecastSectionV2Dto;

@Component
public class ForecastSectionClient {
    private static final Logger log = LoggerFactory.getLogger(ForecastSectionClient.class);

    private static final String URL_TEMPLATE = "{baseUrl}{dataAndVersion}?suid={suid}&user={user}&pass={pass}";
    private static final String KEY_BASE_URL = "baseUrl";
    private static final String KEY_SUID = "suid";
    private static final String KEY_USER = "user";
    private static final String KEY_PASS = "pass";
    private static final String KEY_DATA_AND_VERSION = "dataAndVersion";

    private final Map<String, String> keyValues;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public ForecastSectionClient(final RestTemplate restTemplate,
                                 final ObjectMapper objectMapper,
                                 @Value("${roadConditions.baseUrl}") final String baseUrl,
                                 @Value("${roadConditions.suid}") final String suid,
                                 @Value("${roadConditions.user}") final String user,
                                 @Value("${roadConditions.pass}") final String pass) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        keyValues = new HashMap<>();
        keyValues.put(KEY_BASE_URL, baseUrl);
        keyValues.put(KEY_SUID, suid);
        keyValues.put(KEY_USER, user);
        keyValues.put(KEY_PASS, pass);
    }

    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 30000))
    public List<ForecastSectionCoordinatesDto> getForecastSectionV1Metadata() {
        final LinkedHashMap<String, Object> response = doGetForGzippedObject(getMetadataUrl(1), LinkedHashMap.class);
        return response == null ? Collections.emptyList() : response.entrySet().stream().map(this::mapForecastSectionCoordinates).collect(Collectors.toList());
    }

    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 30000))
    public ForecastSectionV2Dto getForecastSectionV2Metadata() {
        return doGetForGzippedObject(getMetadataUrl(2), ForecastSectionV2Dto.class);
    }

    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 30000))
    public ForecastSectionDataDto getRoadConditions(final int version) {
        return doGetForGzippedObject(getDataUrl(version), ForecastSectionDataDto.class);
    }

    private <T> T doGetForGzippedObject(final String url, Class<T> returnType) {
        final StopWatch timer = StopWatch.createStarted();
        return restTemplate.execute(
            url,
            HttpMethod.GET,
            (ClientHttpRequest requestCallback) -> requestCallback.getHeaders().setContentType(MediaType.APPLICATION_OCTET_STREAM),
            responseExtractor -> { // IOUtils.toString(new GZIPInputStream(responseExtractor.getBody()).readAllBytes());
                final GZIPInputStream gZipIs = new GZIPInputStream(responseExtractor.getBody());
                try {
                    // final String json = IOUtils.toString(gZipIs, StandardCharsets.UTF_8);
                    final ObjectReader reader = objectMapper.readerFor(returnType);
                    return reader.readValue(gZipIs, returnType);
                } finally {
                    try { gZipIs.close(); } catch ( final IOException ignore ) {}
                    try { responseExtractor.getBody().close(); } catch ( final IOException ignore ) {}
                    log.info("method=doGetForGzippedObject {} for type {} tookMs={}", getLoggerSafeUrl(url), returnType.getName(), timer.getTime());
                }
            });
    }

    private String getLoggerSafeUrl(final String url) {
        return StringUtils.substringBefore(url, "?") + "?...";
    }

    // Caused by: com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException: Unrecognized field "00001_001_000_0" (class fi.livi.digitraffic.tie.service.v1.forecastsection.dto.v2.ForecastSectionV2Dto), not marked as ignorable (3 known properties: "type", "features", "dataUpdatedTime"])
    protected ForecastSectionCoordinatesDto mapForecastSectionCoordinates(final Map.Entry<String, Object> forecastSection) {
        final ForecastSectionCoordinatesEntry entry = objectMapper.convertValue(forecastSection.getValue(), ForecastSectionCoordinatesEntry.class);

        return new ForecastSectionCoordinatesDto(forecastSection.getKey(), entry.getName(), entry.getCoordinates());
    }

    private String getMetadataUrl(final int version) {
        return getUrlWithVersion(version, true);
    }

    private String getDataUrl(final int version) {
        return getUrlWithVersion(version, false);
    }

    private String getUrlWithVersion(final int version, boolean metadata) {
        final Map<String, String> values = new HashMap<>();
        values.putAll(keyValues);
        values.put(KEY_DATA_AND_VERSION, getUrlDataPart(version, metadata));
        final StringSubstitutor ss = new StringSubstitutor(values, "{","}");
        return ss.replace(URL_TEMPLATE);
    }

    private static String getUrlDataPart(final int version, final boolean metadata) {
        if (metadata) {
            if (version == 1) {
                return "roadsV1.json.gz";
            } else if (version == 2) {
                return "roadsV2.json.gz";
            } else if (version == 3) {
                return "roadsV3.json.gz";
            }
        } else { // road conditions
            if (version == 1) {
                return "json/keliennuste.json.gz";
            } else if (version == 2) {
                return "keliennuste-v2.json.gz";
            } else if (version == 3) {
                return "keliennuste-v3.json.gz";
            }
        }
        throw new IllegalArgumentException("Unknown version " + version);
    }


}
