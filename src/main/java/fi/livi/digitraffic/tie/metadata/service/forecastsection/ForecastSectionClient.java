package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ForecastSectionClient {

    @Value("${roadConditions.baseUrl}")
    private String baseUrl;

    private final String roadsUrl = "roads.php";

    private final String roadConditionsUrl = "roadConditionsV1-json.php";

    @Autowired
    private RestTemplate restTemplate;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Retryable
    public List<ForecastSectionCoordinatesDto> getForecastSectionMetadata() {

        final LinkedHashMap<String, Object> response = restTemplate.getForObject(baseUrl + roadsUrl, LinkedHashMap.class);

        return response.entrySet().stream().map(this::mapForecastSectionCoordinates).collect(Collectors.toList());
    }

    @Retryable
    public ForecastSectionDataDto getRoadConditions() {

        return restTemplate.getForObject(baseUrl + roadConditionsUrl, ForecastSectionDataDto.class);
    }

    private ForecastSectionCoordinatesDto mapForecastSectionCoordinates(final Map.Entry<String, Object> forecastSection) {
        final ForecastSectionCoordinatesEntry entry = objectMapper.convertValue(forecastSection.getValue(), ForecastSectionCoordinatesEntry.class);

        return new ForecastSectionCoordinatesDto(forecastSection.getKey(), entry.getName(), entry.getCoordinates());
    }
}
