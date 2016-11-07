package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ForecastSectionClient {

    @Value("${roadConditions.baseUrl}")
    private String baseUrl;

    private final String roadsUrl = "roads.php";

    private final String roadConditionsUrl = "roadConditionsV1-json.php";

    private RestTemplate restTemplate = new RestTemplate();

    private ObjectMapper objectMapper = new ObjectMapper();

    public List<ForecastSectionCoordinatesDto> getForecastSectionMetadata() {

        LinkedHashMap<String, Object> response = restTemplate.getForObject(baseUrl + roadsUrl, LinkedHashMap.class);

        return response.entrySet().stream().map(this::mapForecastSectionCoordinates).collect(Collectors.toList());
    }

    public ForecastSectionDataDto getRoadConditions() {

        return restTemplate.getForObject(baseUrl + roadConditionsUrl, ForecastSectionDataDto.class);
    }

    private ForecastSectionCoordinatesDto mapForecastSectionCoordinates(Map.Entry<String, Object> forecastSection) {
        ForecastSectionCoordinatesEntry entry = objectMapper.convertValue(forecastSection.getValue(), ForecastSectionCoordinatesEntry.class);

        return new ForecastSectionCoordinatesDto(forecastSection.getKey(), entry.getName(), entry.getCoordinates());
    }
}
