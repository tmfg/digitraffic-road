package fi.livi.digitraffic.tie.metadata.service.roadconditions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RoadConditionsClient {

    @Value("${roadConditions.baseUrl}")
    private String baseUrl;

    private final String roadsUrl = "roads.php";

    private RestTemplate restTemplate = new RestTemplate();

    private ObjectMapper objectMapper = new ObjectMapper();

    public List<RoadSectionCoordinatesDto> getRoadSections() {

        LinkedHashMap<String, Object> response = restTemplate.getForObject(baseUrl + roadsUrl, LinkedHashMap.class);

        return response.entrySet().stream().map(this::mapRoadSection).collect(Collectors.toList());
    }

    private RoadSectionCoordinatesDto mapRoadSection(Map.Entry<String, Object> RoadSection) {
        RoadSectionCoordinatesEntry coordinatesEntry = objectMapper.convertValue(RoadSection.getValue(), RoadSectionCoordinatesEntry.class);

        return new RoadSectionCoordinatesDto(RoadSection.getKey(), coordinatesEntry.getName(), coordinatesEntry.getCoordinates());
    }
}
