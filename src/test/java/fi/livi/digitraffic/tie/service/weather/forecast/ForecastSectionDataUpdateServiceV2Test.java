package fi.livi.digitraffic.tie.service.weather.forecast;

import fi.livi.digitraffic.tie.conf.amazon.RoadForecastS3Properties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.dao.weather.forecast.ForecastSectionRepository;
import fi.livi.digitraffic.tie.dao.weather.forecast.ForecastSectionWeatherV2Repository;
import fi.livi.digitraffic.tie.dto.weather.forecast.client.ForecastSectionDataDtoV2;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.weather.forecast.ForecastSectionType;
import fi.livi.digitraffic.tie.model.weather.forecast.v2.ForecastSectionWeatherV2;
import fi.livi.digitraffic.tie.model.weather.forecast.v2.ForecastTypeV2;
import fi.livi.digitraffic.tie.model.weather.forecast.v2.RoadConditionV2;
import fi.livi.digitraffic.tie.service.DataStatusService;
import jakarta.persistence.EntityManager;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import tools.jackson.databind.ObjectMapper;

public class ForecastSectionDataUpdateServiceV2Test extends AbstractServiceTest {
    private static final String KNOWN = "TEST_V2_KNOWN";
    private static final String OBSOLETE_VERSION = "TEST_V2_V1SECTION";

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;
    @Autowired
    private ForecastSectionWeatherV2Repository weatherRepository;
    @Autowired
    private DataStatusService dataStatusService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EntityManager entityManager;

    private ForecastSectionDataUpdateServiceV2 service;
    private long knownId;

    @BeforeEach
    public void init() {
        service = new ForecastSectionDataUpdateServiceV2(forecastSectionRepository, weatherRepository, dataStatusService);
        knownId = createSection(KNOWN, ForecastSectionType.DETAILED);
        createSection(OBSOLETE_VERSION, ForecastSectionType.SIMPLE);
        dataStatusService.updateDataUpdated(DataType.FORECAST_SECTION_WEATHER_DATA_V2, Instant.parse("2000-01-01T00:00:00Z"));
    }

    private long createSection(final String naturalId, final ForecastSectionType type) {
        final Number id = (Number) entityManager.createNativeQuery("SELECT nextval('seq_forecast_section')").getSingleResult();
        entityManager.createNativeQuery("INSERT INTO forecast_section(id, natural_id, description, version, type, road_number, road_section_number, road_section_version_number) VALUES (:id, :nid, 'test', :ver, :v, 1, 1, 1)")
            .setParameter("id", id.longValue()).setParameter("nid", naturalId).setParameter("ver", type == ForecastSectionType.SIMPLE ? 1 : 2).setParameter("v", type.name()).executeUpdate();
        return id.longValue();
    }

    private static String forecast(final String name, final String type, final String temp, final int roadCondition) {
        return """
            {"time":"2026-10-07T12:00:00Z","type":"%s","forecastName":"%s","daylight":true,"roadTemperature":%s,"temperature":%s,
             "windSpeed":1.5,"windDirection":90,"overallRoadCondition":1,"roadCondition":%d,"weatherSymbol":1,"reliability":null}
            """.formatted(type, name, temp, temp, roadCondition);
    }

    private ForecastSectionDataDtoV2 data(final String updated, final String temp, final List<String> names, final String... otherSectionIds) {
        final String forecasts = names.stream().map(n -> forecast(n, "0h".equals(n) ? "OBSERVATION" : "FORECAST", temp, 1)).collect(Collectors.joining(","));
        final StringBuilder sections = new StringBuilder("{\"id\":\"" + KNOWN + "\",\"forecasts\":[" + forecasts + "]}");
        for (final String other : otherSectionIds) {
            sections.append(",{\"id\":\"").append(other).append("\",\"forecasts\":[").append(forecasts).append("]}");
        }
        return objectMapper.readValue("{\"dataUpdatedTime\":\"" + updated + "\",\"forecastSections\":[" + sections + "]}", ForecastSectionDataDtoV2.class);
    }

    private List<ForecastSectionWeatherV2> stored() {
        entityManager.flush();
        entityManager.clear();
        return weatherRepository.findByForecastSectionIds(List.of(knownId));
    }

    @Test
    public void insertsKnownSectionsAndIgnoresUnknownAndOtherNetworkSections() {
        assertTrue(service.updateForecastSectionWeatherData(
            data("2026-10-07T11:05:48Z", "10.5", List.of("0h", "2h", "4h"), "TEST_V2_UNKNOWN", OBSOLETE_VERSION)));

        assertEquals(3, stored().size());
        assertEquals(3, weatherRepository.findAll().stream().filter(w -> w.getForecastSectionId() == knownId).count());
        assertEquals(Instant.parse("2026-10-07T11:05:48Z"), dataStatusService.findDataUpdatedTime(DataType.FORECAST_SECTION_WEATHER_DATA_V2));
        final ForecastSectionWeatherV2 obs = stored().stream().filter(w -> w.getForecastName().equals("0h")).findFirst().orElseThrow();
        assertEquals(ForecastTypeV2.OBSERVATION, obs.getType());
        assertEquals(0, new BigDecimal("10.5").compareTo(obs.getTemperature()));
        assertEquals(RoadConditionV2.DRY, obs.getRoadCondition());
    }

    @Test
    public void updatesExistingAndDeletesMissingForecasts() {
        service.updateForecastSectionWeatherData(data("2026-10-07T11:05:48Z", "10.5", List.of("0h", "2h", "4h")));
        assertTrue(service.updateForecastSectionWeatherData(data("2026-10-07T12:05:48Z", "-3.0", List.of("0h", "2h"))));

        final List<ForecastSectionWeatherV2> rows = stored();
        assertEquals(List.of("0h", "2h"), rows.stream().map(ForecastSectionWeatherV2::getForecastName).sorted().toList());
        rows.forEach(w -> assertEquals(0, new BigDecimal("-3.0").compareTo(w.getTemperature())));
        assertEquals(Instant.parse("2026-10-07T12:05:48Z"), dataStatusService.findDataUpdatedTime(DataType.FORECAST_SECTION_WEATHER_DATA_V2));
    }

    @Test
    public void skipsOldOrSameData() {
        service.updateForecastSectionWeatherData(data("2026-10-07T11:05:48Z", "10.5", List.of("0h")));
        assertFalse(service.updateForecastSectionWeatherData(data("2026-10-07T11:05:48Z", "1.0", List.of("0h"))));
        assertFalse(service.updateForecastSectionWeatherData(data("2026-10-07T10:00:00Z", "1.0", List.of("0h"))));

        assertEquals(0, new BigDecimal("10.5").compareTo(stored().getFirst().getTemperature()));
    }

    @Test
    public void skipsInvalidForecastsWithoutFailing() {
        final ForecastSectionDataDtoV2 d = objectMapper.readValue("""
            {"dataUpdatedTime":"2026-10-07T11:05:48Z","forecastSections":[{"id":"%s","forecasts":[
              {"time":"2026-10-07T12:00:00Z","type":"FORECAST","forecastName":"2h"},
              %s]}]}""".formatted(KNOWN, forecast("4h", "FORECAST", "1.0", 1)), ForecastSectionDataDtoV2.class);
        assertTrue(service.updateForecastSectionWeatherData(d));
        assertEquals(List.of("4h"), stored().stream().map(ForecastSectionWeatherV2::getForecastName).toList());
    }

    @Test
    public void unknownTypeSkipsForecastAndUnknownConditionIsStoredAsNull() {
        final ForecastSectionDataDtoV2 d = objectMapper.readValue("""
            {"dataUpdatedTime":"2026-10-07T11:05:48Z","forecastSections":[{"id":"%s","forecasts":[
              %s,
              %s]}]}""".formatted(KNOWN, forecast("2h", "PREDICTION", "1.0", 1), forecast("4h", "FORECAST", "1.0", 999)), ForecastSectionDataDtoV2.class);
        assertTrue(service.updateForecastSectionWeatherData(d));

        final List<ForecastSectionWeatherV2> rows = stored();
        assertEquals(List.of("4h"), rows.stream().map(ForecastSectionWeatherV2::getForecastName).toList());
        assertNull(rows.getFirst().getRoadCondition());
    }

    @Test
    public void updaterMarksDataCheckedEvenWhenNoFilesFound() {
        final S3Client s3 = mock(S3Client.class);
        when(s3.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(ListObjectsV2Response.builder().build());
        final ForecastSectionDataUpdaterV2 updater = new ForecastSectionDataUpdaterV2(
            new ForecastSectionS3ClientV2(s3, objectMapper, new RoadForecastS3Properties("bucket", "eu-north-1", "forecast/", 24)), service, dataStatusService);

        assertNull(dataStatusService.findDataUpdatedTime(DataType.FORECAST_SECTION_WEATHER_DATA_V2_CHECK));
        assertNull(updater.updateForecastSectionWeatherData());
        assertNotNull(dataStatusService.findDataUpdatedTime(DataType.FORECAST_SECTION_WEATHER_DATA_V2_CHECK));
    }

    @Test
    public void updaterReadsLatestFileFromS3AndSkipsProcessedOnes() {
        final S3Client s3 = mock(S3Client.class);
        final String key = "forecast/roadweather_forecast_public_20261007T120000Z_20261007T110548Z.json";
        when(s3.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(
            ListObjectsV2Response.builder().contents(S3Object.builder().key(key).build()).build());
        final String json = "{\"dataUpdatedTime\":\"2026-10-07T11:05:48Z\",\"forecastSections\":[{\"id\":\"" + KNOWN + "\",\"forecasts\":["
            + forecast("0h", "OBSERVATION", "5.0", 2) + "]}]}";
        when(s3.getObject(any(GetObjectRequest.class), any(ResponseTransformer.class)))
            .thenReturn(ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), json.getBytes(StandardCharsets.UTF_8)));
        final ForecastSectionDataUpdaterV2 updater = new ForecastSectionDataUpdaterV2(
            new ForecastSectionS3ClientV2(s3, objectMapper, new RoadForecastS3Properties("bucket", "eu-north-1", "forecast/", 24)), service, dataStatusService);

        assertEquals(Instant.parse("2026-10-07T11:05:48Z"), updater.updateForecastSectionWeatherData());
        assertEquals(RoadConditionV2.MOIST, stored().getFirst().getRoadCondition());
        assertNull(updater.updateForecastSectionWeatherData());
    }
}
