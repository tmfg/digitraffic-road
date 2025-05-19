package fi.livi.digitraffic.tie.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import fi.livi.digitraffic.common.util.StringUtil;
import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.helper.WazeReverseGeocodingApi;
import fi.livi.digitraffic.tie.metadata.geojson.MultiLineString;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.service.waze.WazeReverseGeocodingService;

public class WazeReverseGeocodingServiceTest extends AbstractRestWebTest {

    @Autowired
    private WazeReverseGeocodingService wazeReverseGeocodingService;

    @MockitoBean
    private WazeReverseGeocodingApi wazeReverseGeocodingApi;

    @BeforeEach
    public void beforeEach() {
        wazeReverseGeocodingService.evictCache();
    }

    @Test
    public void shouldReturnNearestStreetName() {
        final String streetName = "correct street name 123";
        final String responseTemplate =
                "{\"lat\":1.1,\"lon\":1.1,\"radius\":0,\"result\":[{\"distance\":100,\"names\":[\"foo\",\"bar\"]},{\"distance\":10,\"names\":[\"%s\",\"baz\"]}]}";
        final String RESPONSE = String.format(responseTemplate, streetName);

        when(this.wazeReverseGeocodingApi.fetch(anyDouble(), anyDouble())).thenReturn(Optional.of(RESPONSE));
        final var result = wazeReverseGeocodingService.getStreetName(new Point(1, 2));

        assertTrue(result.isPresent());
        assertEquals(streetName, result.orElse(null));
    }

    @Test
    void testComplexMultiLineString() {
        final List<List<Double>> coords = List.of(
                List.of(22.349803, 60.328812),
                List.of(22.349665, 60.328727),
                List.of(22.34953, 60.328633),
                List.of(22.349066, 60.328309),
                List.of(22.348426, 60.327777),
                List.of(22.347803, 60.327145),
                List.of(22.34704, 60.326269),
                List.of(22.346586, 60.325773),
                List.of(22.346208, 60.325391),
                List.of(22.346033, 60.325249),
                List.of(22.345897, 60.325133),
                List.of(22.345753, 60.32502),
                List.of(22.345328, 60.324685),
                List.of(22.344628, 60.324187),
                List.of(22.343959, 60.323709),
                List.of(22.343006, 60.323043),
                List.of(22.342822, 60.322916),
                List.of(22.342138, 60.322438),
                List.of(22.341466, 60.321962),
                List.of(22.340715, 60.321397),
                List.of(22.339964, 60.32079),
                List.of(22.33934, 60.320294),
                List.of(22.339236, 60.320214),
                List.of(22.338684, 60.319791),
                List.of(22.338526, 60.31966),
                List.of(22.338215, 60.319401),
                List.of(22.337886, 60.319109),
                List.of(22.337289, 60.31861),
                List.of(22.33637, 60.317928),
                List.of(22.335479, 60.317408),
                List.of(22.334491, 60.316863),
                List.of(22.334004, 60.316607),
                List.of(22.333678, 60.316437),
                List.of(22.333537, 60.316363),
                List.of(22.333279, 60.316237),
                List.of(22.333035, 60.316131),
                List.of(22.332985, 60.316117)
        );

        final MultiLineString geometry = new MultiLineString();
        geometry.addLineString(coords);

        when(this.wazeReverseGeocodingApi.fetch(anyDouble(), anyDouble())).thenReturn(Optional.empty());
        wazeReverseGeocodingService.getStreetName(geometry);

        verify(this.wazeReverseGeocodingApi, times(1)).fetch(coords.get(18).get(1), coords.get(18).getFirst());

    }

    @Test
    void shouldUseTheMiddleCoordinatePairFromMultiLineString() {
        final double latitude1 = 26.26;
        final double longitude1 = 65.65;
        final double latitude2 = 24.24;
        final double longitude2 = 63.63;

        final List<List<Double>> coords = List.of(List.of(longitude1, latitude1), List.of(longitude2, latitude2));
        final MultiLineString geometry = new MultiLineString();
        geometry.addLineString(coords);

        when(this.wazeReverseGeocodingApi.fetch(anyDouble(), anyDouble())).thenReturn(Optional.empty());
        wazeReverseGeocodingService.getStreetName(geometry);

        verify(this.wazeReverseGeocodingApi, times(1)).fetch(latitude2, longitude2);
    }

    @Test
    void testEmptyMultilineString() {
        final MultiLineString geometry = new MultiLineString();

        wazeReverseGeocodingService.getStreetName(geometry);

        verify(this.wazeReverseGeocodingApi, times(0)).fetch(anyDouble(), anyDouble());
    }

    @Test
    void testMultilineStringWithEmptyLinestring() {
        final MultiLineString geometry = new MultiLineString(List.of(List.of()));

        wazeReverseGeocodingService.getStreetName(geometry);

        verify(this.wazeReverseGeocodingApi, times(0)).fetch(anyDouble(), anyDouble());
    }

    @Test
    public void shouldCacheReverseGeocodingResponses() {
        final double latitude = 60.1;
        final double longitude = 21.3;
        final Point geometry = new Point(longitude, latitude);
        final String streetName = "street name 123";
        final String responseTemplate =
                "{\"lat\":1,\"lon\":2,\"radius\":50,\"result\":[{\"distance\":3.1415,\"names\":[\"%s\"]},{\"distance\":20.2,\"names\":[\"Lautta\"]}]}";
        final String response = String.format(responseTemplate, streetName);
        when(this.wazeReverseGeocodingApi.fetch(anyDouble(), anyDouble())).thenReturn(Optional.of(response));

        final Optional<String> maybeStreetName1 = this.wazeReverseGeocodingService.getStreetName(geometry);
        final Optional<String> maybeStreetName2 = this.wazeReverseGeocodingService.getStreetName(geometry);

        assertNotNull(maybeStreetName1);
        assertTrue(maybeStreetName1.isPresent());
        assertNotNull(maybeStreetName2);
        assertTrue(maybeStreetName2.isPresent());

        assertEquals(streetName, maybeStreetName1.get());
        assertEquals(maybeStreetName1.get(), maybeStreetName2.get());

        verify(this.wazeReverseGeocodingApi, times(1)).fetch(latitude, longitude);
    }

    @Test
    public void shouldHandleIncorrectReverseGeocodingResults() {
        when(this.wazeReverseGeocodingApi.fetch(anyDouble(), anyDouble())).thenReturn(Optional.of(""));

        final Optional<String> reverseGeocode = wazeReverseGeocodingService.getStreetName(new Point(1, 2));

        assertNotNull(reverseGeocode);
        assertTrue(reverseGeocode.isEmpty());
    }

    @Test
    public void cacheWorksReturnNearestStreetName() {
        final Point geometry = new Point(1, 2);
        final String streetNameOld = "Street before";
        final String streetNameUpdated = "Street after";
        final String responseTemplate =
                "{\"lat\":1.1,\"lon\":1.1,\"radius\":0,\"result\":[{\"distance\":100,\"names\":[\"foo\",\"bar\"]},{\"distance\":10,\"names\":[\"{}\",\"baz\"]}]}";
        final String RESPONSE_OLD = StringUtil.format(responseTemplate, streetNameOld);
        final String RESPONSE_NEW = StringUtil.format(responseTemplate, streetNameUpdated);

        // First call goes to api and gets cached
        when(this.wazeReverseGeocodingApi.fetch(anyDouble(), anyDouble())).thenReturn(Optional.of(RESPONSE_OLD));
        final Optional<String> oldResultFromApi = wazeReverseGeocodingService.getStreetName(geometry);
        assertEquals(streetNameOld, oldResultFromApi.orElse(null));

        // Update new value to api
        when(this.wazeReverseGeocodingApi.fetch(anyDouble(), anyDouble())).thenReturn(Optional.of(RESPONSE_NEW));

        // Second call comes from cache and equals with previous
        final Optional<String> oldResultFromCache = wazeReverseGeocodingService.getStreetName(geometry);
        assertEquals(streetNameOld, oldResultFromApi.orElse(null));
        assertEquals(oldResultFromApi.orElseThrow(), oldResultFromCache.orElseThrow());

        // After cache evict third call should go to api
        wazeReverseGeocodingService.evictCache();
        final Optional<String> newResultFromApi = wazeReverseGeocodingService.getStreetName(geometry);
        assertEquals(streetNameUpdated, newResultFromApi.orElse(null));
        assertNotEquals(oldResultFromApi.orElseThrow(), newResultFromApi.orElseThrow());
    }
}
