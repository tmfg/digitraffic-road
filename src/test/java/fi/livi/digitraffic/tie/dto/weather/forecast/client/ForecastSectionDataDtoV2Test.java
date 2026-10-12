package fi.livi.digitraffic.tie.dto.weather.forecast.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import fi.livi.digitraffic.tie.model.weather.forecast.v2.ForecastTypeV2;
import fi.livi.digitraffic.tie.model.weather.forecast.v2.OverallRoadConditionV2;
import fi.livi.digitraffic.tie.model.weather.forecast.v2.RoadConditionV2;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

public class ForecastSectionDataDtoV2Test {

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    public void readSampleFile() throws IOException {
        final String json = new ClassPathResource("forecastsection/forecast-v2-public-sample.json").getContentAsString(StandardCharsets.UTF_8);
        final ForecastSectionDataDtoV2 data = objectMapper.readValue(json, ForecastSectionDataDtoV2.class);

        assertNotNull(data.dataUpdatedTime);
        assertEquals(2, data.forecastSections.size());

        final ForecastSectionWeatherDtoV2 section = data.forecastSections.getFirst();
        assertEquals("00001_001_00000_2_0", section.naturalId);
        assertEquals(5, section.forecasts.size());

        final ForecastSectionForecastDtoV2 observation = section.forecasts.getFirst();
        assertEquals("0h", observation.forecastName);
        assertEquals(ForecastTypeV2.OBSERVATION, observation.type);
        assertNotNull(observation.time);
        assertNotNull(observation.daylight);
        assertNotNull(observation.roadTemperature);
        assertNull(observation.reliability);
        assertTrue(section.forecasts.stream().skip(1).allMatch(f -> f.type == ForecastTypeV2.FORECAST));
    }

    @Test
    public void nullsZeroCodesAndUnknownFields() {
        final String json = """
            {"dataUpdatedTime":"2026-10-02T11:19:35Z","somethingNew":1,"forecastSections":[
              {"id":"A","extra":true,"forecasts":[
                {"time":"2026-10-02T12:00:00Z","type":"OBSERVATION","forecastName":"0h","daylight":false,
                 "roadTemperature":null,"temperature":-3.5,"windSpeed":null,"windDirection":null,
                 "overallRoadCondition":0,"roadCondition":0,"weatherSymbol":null,"reliability":"X","waterStorage":0.0}
              ]}
            ]}""";
        final ForecastSectionForecastDtoV2 f = objectMapper.readValue(json, ForecastSectionDataDtoV2.class).forecastSections.getFirst().forecasts.getFirst();

        assertEquals(Instant.parse("2026-10-02T11:19:35Z"), objectMapper.readValue(json, ForecastSectionDataDtoV2.class).dataUpdatedTime);
        assertNull(f.roadTemperature);
        assertEquals(0, new BigDecimal("-3.5").compareTo(f.temperature));
        assertNull(f.windSpeed);
        assertNull(f.weatherSymbol);
        assertEquals(OverallRoadConditionV2.NOT_KNOWN, f.overallRoadCondition);
        assertEquals(RoadConditionV2.NOT_KNOWN, f.roadCondition);
        assertEquals("X", f.reliability);
    }

    @Test
    public void unknownCodeIsNull() {
        final String json = """
            {"forecastSections":[{"id":"A","forecasts":[
              {"time":"2026-10-02T12:00:00Z","type":"FORECAST","forecastName":"2h","daylight":true,"overallRoadCondition":99,"roadCondition":42}
            ]}]}""";
        final ForecastSectionForecastDtoV2 f = objectMapper.readValue(json, ForecastSectionDataDtoV2.class).forecastSections.getFirst().forecasts.getFirst();
        assertNull(f.overallRoadCondition);
        assertNull(f.roadCondition);
    }
}
