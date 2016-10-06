package fi.livi.digitraffic.tie.metadata.service.roadconditions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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

        LinkedHashMap<String, Object> response = get(roadsUrl, LinkedHashMap.class);

        return response.entrySet().stream().map(entry -> mapRoadSection(entry)).collect(Collectors.toList());
    }

    private RoadSectionCoordinatesDto mapRoadSection(Map.Entry<String, Object> RoadSection) {
        RoadSectionCoordinatesEntry entryValue = objectMapper.convertValue(RoadSection.getValue(), RoadSectionCoordinatesEntry.class);
        return new RoadSectionCoordinatesDto(RoadSection.getKey(), entryValue.getName(), entryValue.getCoordinates());
    }

    private <T> T get(String url, Class<T> responseType) {
        return restTemplate.exchange(baseUrl + url, HttpMethod.GET, requestEntity(null), responseType).getBody();
    }

    private <T> HttpEntity<T> requestEntity(T request) {
        HttpHeaders headers = new HttpHeaders();

        return new HttpEntity<T>(request, headers);
    }
}
