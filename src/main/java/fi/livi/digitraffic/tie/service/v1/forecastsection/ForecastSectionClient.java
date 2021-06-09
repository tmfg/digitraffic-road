package fi.livi.digitraffic.tie.service.v1.forecastsection;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.service.v1.forecastsection.dto.v1.ForecastSectionCoordinatesDto;
import fi.livi.digitraffic.tie.service.v1.forecastsection.dto.v1.ForecastSectionCoordinatesEntry;
import fi.livi.digitraffic.tie.service.v1.forecastsection.dto.v2.ForecastSectionV2Dto;

@Component
public class ForecastSectionClient {
    private static final String URL_ROADS_V1_PART = "roads.php";
    private static final String URL_ROADS_V2_PART = "roadsV2.php";
    private static final String URL_ROAD_CONDITIONS_V1_PART = "roadConditionsV1-json.php";
    private static final String URL_ROAD_CONDITIONS_V2_PART = "roadConditionsV2-json.php";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String baseUrl;
    private final RestTemplate restTemplate;

    public ForecastSectionClient(final RestTemplate restTemplate, @Value("${roadConditions.baseUrl}") final String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Retryable
    public List<ForecastSectionCoordinatesDto> getForecastSectionV1Metadata() {
        final LinkedHashMap<String, Object> response = restTemplate.getForObject(baseUrl + URL_ROADS_V1_PART, LinkedHashMap.class);

        return response.entrySet().stream().map(this::mapForecastSectionCoordinates).collect(Collectors.toList());
    }

    @Retryable
    public ForecastSectionDataDto getRoadConditions(final int version) {
        if (version == 1) {
            return restTemplate.getForObject(baseUrl + URL_ROAD_CONDITIONS_V1_PART, ForecastSectionDataDto.class);
        } else {
            return restTemplate.getForObject(baseUrl + URL_ROAD_CONDITIONS_V2_PART, ForecastSectionDataDto.class);
        }
    }

    protected ForecastSectionCoordinatesDto mapForecastSectionCoordinates(final Map.Entry<String, Object> forecastSection) {
        final ForecastSectionCoordinatesEntry entry = objectMapper.convertValue(forecastSection.getValue(), ForecastSectionCoordinatesEntry.class);

        return new ForecastSectionCoordinatesDto(forecastSection.getKey(), entry.getName(), entry.getCoordinates());
    }

    public ForecastSectionV2Dto getForecastSectionV2Metadata() {
        return restTemplate.getForObject(baseUrl + URL_ROADS_V2_PART, ForecastSectionV2Dto.class);
    }
}
