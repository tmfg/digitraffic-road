package fi.livi.digitraffic.tie.service.v1.forecastsection;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.service.RestTemplateGzipService;
import fi.livi.digitraffic.tie.service.v1.forecastsection.dto.v1.ForecastSectionCoordinatesDto;
import fi.livi.digitraffic.tie.service.v1.forecastsection.dto.v1.ForecastSectionCoordinatesEntry;
import fi.livi.digitraffic.tie.service.v1.forecastsection.dto.v2.ForecastSectionV2Dto;

@ConditionalOnNotWebApplication
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
    private RestTemplateGzipService restTemplateGzipService;

    public ForecastSectionClient(final RestTemplateGzipService restTemplateGzipService,
                                 final ObjectMapper objectMapper,
                                 @Value("${roadConditions.baseUrl}") final String baseUrl,
                                 @Value("${roadConditions.suid}") final String suid,
                                 @Value("${roadConditions.user}") final String user,
                                 @Value("${roadConditions.pass}") final String pass) {
        this.objectMapper = objectMapper;
        this.restTemplateGzipService = restTemplateGzipService;
        keyValues = new HashMap<>();
        keyValues.put(KEY_BASE_URL, baseUrl);
        keyValues.put(KEY_SUID, suid);
        keyValues.put(KEY_USER, user);
        keyValues.put(KEY_PASS, pass);
    }

    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 30000))
    public List<ForecastSectionCoordinatesDto> getForecastSectionV1Metadata() {
        final LinkedHashMap<String, Object> response =
            restTemplateGzipService.getForGzippedObject(getMetadataUrl(1), LinkedHashMap.class,
                                                        "getForecastSectionV1Metadata", getMetadataUrlLoggerSafe(1));
        return response == null ? Collections.emptyList() : response.entrySet().stream().map(this::mapForecastSectionCoordinates).collect(Collectors.toList());
    }

    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 30000))
    public ForecastSectionV2Dto getForecastSectionV2Metadata() {
        return restTemplateGzipService.getForGzippedObject(getMetadataUrl(2), ForecastSectionV2Dto.class,
                                                           "getForecastSectionV2Metadata", getMetadataUrlLoggerSafe(2));
    }

    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 30000))
    public ForecastSectionDataDto getRoadConditions(final int version) {
        return restTemplateGzipService.getForGzippedObject(getDataUrl(version), ForecastSectionDataDto.class,
                                                           "getRoadConditionsV" + version, getDataUrlLoggerSafe(version));
    }

    protected ForecastSectionCoordinatesDto mapForecastSectionCoordinates(final Map.Entry<String, Object> forecastSection) {
        final ForecastSectionCoordinatesEntry entry = objectMapper.convertValue(forecastSection.getValue(), ForecastSectionCoordinatesEntry.class);

        return new ForecastSectionCoordinatesDto(forecastSection.getKey(), entry.getName(), entry.getCoordinates());
    }

    private String getMetadataUrl(final int version) {
        return getUrlWithVersion(version, true, false);
    }

    private String getMetadataUrlLoggerSafe(final int version) {
        return getUrlWithVersion(version, true, true);
    }

    private String getDataUrl(final int version) {
        return getUrlWithVersion(version, false, false);
    }

    private String getDataUrlLoggerSafe(final int version) {
        return getUrlWithVersion(version, false, true);
    }

    private String getUrlWithVersion(final int version, boolean metadata, boolean loggerSafe) {
        final Map<String, String> values = new HashMap<>();
        if (!loggerSafe) {
            values.putAll(keyValues);
        } else {
            values.put(KEY_BASE_URL, keyValues.get(KEY_BASE_URL));
        }
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
