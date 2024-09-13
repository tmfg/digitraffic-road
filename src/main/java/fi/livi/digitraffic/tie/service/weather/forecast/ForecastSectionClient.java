package fi.livi.digitraffic.tie.service.weather.forecast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fi.livi.digitraffic.tie.dto.weather.forecast.client.ForecastSectionCoordinatesDto;
import fi.livi.digitraffic.tie.dto.weather.forecast.client.ForecastSectionCoordinatesEntry;
import fi.livi.digitraffic.tie.dto.weather.forecast.client.ForecastSectionDataDto;
import fi.livi.digitraffic.tie.dto.weather.forecast.client.ForecastSectionV2Dto;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

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

    private final WebClient webClient;

    public ForecastSectionClient(final WebClient webClient,
                                 final ObjectMapper objectMapper,
                                 @Value("${roadConditions.baseUrl}") final String baseUrl,
                                 @Value("${roadConditions.suid}") final String suid,
                                 @Value("${roadConditions.user}") final String user,
                                 @Value("${roadConditions.pass}") final String pass) {
        this.objectMapper = objectMapper;
        this.webClient = webClient;

        this.objectMapper.registerModule(new JavaTimeModule());

        keyValues = Map.of(
            KEY_BASE_URL, baseUrl,
            KEY_SUID, suid,
            KEY_USER, user,
            KEY_PASS, pass);
    }

    private <T> T getFromUrl(final String url, final Class<T> clazz) {
        final DataBuffer db = webClient.get().uri(url)
            .accept(MediaType.APPLICATION_OCTET_STREAM)
            .retrieve().bodyToMono(DataBuffer.class).block();

        // ok, DataBuffer holds the response that is gzipped

        try(final InputStream dbIs = db.asInputStream(); final GZIPInputStream gis = new GZIPInputStream(dbIs)) {
            final T object = objectMapper.readerFor(clazz).readValue(gis);

            return object;
        } catch (final Exception e) {
            log.error("Error converting response", e);

            return null;
        } finally {
            DataBufferUtils.release(db);
        }
    }

    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 30000))
    public List<ForecastSectionCoordinatesDto> getForecastSectionV1Metadata() {
        final LinkedHashMap<String, Object> response = getFromUrl(getMetadataUrl(1), LinkedHashMap.class);

        return response == null ? Collections.emptyList() : response.entrySet().stream().map(this::mapForecastSectionCoordinates).collect(Collectors.toList());
    }

    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 30000))
    public ForecastSectionV2Dto getForecastSectionV2Metadata() {
        return getFromUrl(getMetadataUrl(2), ForecastSectionV2Dto.class);
    }

    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 30000))
    public ForecastSectionDataDto getRoadConditions(final int version) {
        return getFromUrl(getDataUrl(version), ForecastSectionDataDto.class);
    }

    protected ForecastSectionCoordinatesDto mapForecastSectionCoordinates(final Map.Entry<String, Object> forecastSection) {
        final ForecastSectionCoordinatesEntry entry = objectMapper.convertValue(forecastSection.getValue(), ForecastSectionCoordinatesEntry.class);

        return new ForecastSectionCoordinatesDto(forecastSection.getKey(), entry.getName(), entry.getCoordinates());
    }

    private String getMetadataUrl(final int version) {
        return getUrlWithVersion(version, true, false);
    }

    private String getDataUrl(final int version) {
        return getUrlWithVersion(version, false, false);
    }

    private String getUrlWithVersion(final int version, final boolean metadata, final boolean loggerSafe) {
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
