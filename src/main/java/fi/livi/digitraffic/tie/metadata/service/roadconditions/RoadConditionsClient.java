package fi.livi.digitraffic.tie.metadata.service.roadconditions;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.ForecastSectionNaturalIdHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RoadConditionsClient {

    @Value("${roadConditions.baseUrl}")
    private String baseUrl;

    private final String roadsUrl = "roads.php";

    private final String roadConditionsUrl = "roadconditions.php";

    private RestTemplate restTemplate = new RestTemplate();

    private ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    private void postConstruct() {
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm"));
    }

    public List<RoadSectionCoordinatesDto> getRoadSections() {

        LinkedHashMap<String, Object> response = restTemplate.getForObject(baseUrl + roadsUrl, LinkedHashMap.class);

        return response.entrySet().stream().map(this::mapRoadSection).collect(Collectors.toList());
    }

    public List<RoadSectionWeatherDto> getRoadSectionWeather() {

        LinkedHashMap<String, Object> response = restTemplate.getForObject(baseUrl + roadConditionsUrl, LinkedHashMap.class);

        return response.entrySet().stream()
                .filter(e -> ForecastSectionNaturalIdHelper.isNaturalId(e.getKey()))
                .map(this::mapRoadSectionWeather).collect(Collectors.toList());
    }

    private RoadSectionCoordinatesDto mapRoadSection(Map.Entry<String, Object> RoadSection) {
        RoadSectionCoordinatesEntry coordinatesEntry = objectMapper.convertValue(RoadSection.getValue(), RoadSectionCoordinatesEntry.class);

        return new RoadSectionCoordinatesDto(RoadSection.getKey(), coordinatesEntry.getName(), coordinatesEntry.getCoordinates());
    }

    private RoadSectionWeatherDto mapRoadSectionWeather(Map.Entry<String, Object> roadWeather) {
        RoadSectionWeatherEntry roadSectionWeatherEntry = objectMapper.convertValue(roadWeather.getValue(), RoadSectionWeatherEntry.class);

        return new RoadSectionWeatherDto(roadWeather.getKey(), roadSectionWeatherEntry.roadName, roadSectionWeatherEntry.weatherForecasts);
    }
}
