package fi.livi.digitraffic.tie.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.springframework.boot.test.mock.mockito.MockBean;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.helper.WazeReverseGeocodingApi;
import fi.livi.digitraffic.tie.metadata.geojson.MultiLineString;
import fi.livi.digitraffic.tie.metadata.geojson.Point;

public class WazeReverseGeocodingServiceTest extends AbstractRestWebTest {

    @Autowired
    private WazeReverseGeocodingService wazeReverseGeocodingService;

    @MockBean
    private WazeReverseGeocodingApi wazeReverseGeocodingApi;

    @BeforeEach
    public void beforeEach() {
        wazeReverseGeocodingService.evictCache();
    }

    @Test
    public void shouldReturnNearestStreetName() {
        final String streetName = "correct street name 123";
        final String responseTemplate = "{\"lat\":1.1,\"lon\":1.1,\"radius\":0,\"result\":[{\"distance\":100,\"names\":[\"foo\",\"bar\"]},{\"distance\":10,\"names\":[\"%s\",\"baz\"]}]}";
        final String RESPONSE = String.format(responseTemplate, streetName);

        when(this.wazeReverseGeocodingApi.fetch(anyDouble(), anyDouble())).thenReturn(Optional.of(RESPONSE));
        final var result = wazeReverseGeocodingService.getStreetName(new Point(1, 2));

        assertTrue(result.isPresent());
        assertEquals(streetName, result.orElse(null));
    }

    @Test void shouldUseTheFirstCoordinatePairFromMultiLineString() {
        final double latitude = 26.26;
        final double longitude = 65.65;
        final List<List<Double>> coords = List.of(List.of(longitude, latitude), List.of(24.24, 63.63));
        final MultiLineString geometry = new MultiLineString();
        geometry.addLineString(coords);

        when(this.wazeReverseGeocodingApi.fetch(anyDouble(), anyDouble())).thenReturn(Optional.empty());
        wazeReverseGeocodingService.getStreetName(geometry);

        verify(this.wazeReverseGeocodingApi, times(1)).fetch(latitude, longitude);
    }

    @Test
    public void shouldCacheReverseGeocodingResponses() {
        final double latitude = 60.1;
        final double longitude = 21.3;
        final Point geometry = new Point(longitude, latitude);
        final String streetName = "street name 123";
        final String responseTemplate = "{\"lat\":1,\"lon\":2,\"radius\":50,\"result\":[{\"distance\":3.1415,\"names\":[\"%s\"]},{\"distance\":20.2,\"names\":[\"Lautta\"]}]}";
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
}