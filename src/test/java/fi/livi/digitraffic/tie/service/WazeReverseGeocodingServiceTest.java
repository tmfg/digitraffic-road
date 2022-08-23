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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.dto.wazefeed.ReverseGeocode;
import fi.livi.digitraffic.tie.dto.wazefeed.ReverseGeocodeResult;
import fi.livi.digitraffic.tie.helper.WazeReverseGeocodingApi;
import fi.livi.digitraffic.tie.metadata.geojson.MultiLineString;
import fi.livi.digitraffic.tie.metadata.geojson.Point;

public class WazeReverseGeocodingServiceTest extends AbstractRestWebTest {

    @Autowired
    private WazeReverseGeocodingService wazeReverseGeocodingService;

    @MockBean
    private WazeReverseGeocodingApi wazeReverseGeocodingApi;

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
    public void shouldConvertReverseGeocodingResponseToReverseGeocodeResult() {
        final int RADIUS = 50;
        final double DISTANCE = 2.242342;
        final String NAME = "Lautta";

        final String responseTemplate = "{\"lat\":60.542603,\"lon\":21.336319,\"radius\":%s,\"result\":[{\"distance\":%s,\"names\":[\"%s\"]},{\"distance\":20.24164959825527,\"names\":[\"192 - Kivimaantie\"]}]}";
        final String response = String.format(responseTemplate, RADIUS, DISTANCE, NAME);

        final Optional<ReverseGeocode> maybeReverseGeocode = wazeReverseGeocodingService.parseReverseGeocodeJson(response);

        assertNotNull(maybeReverseGeocode);
        assertTrue(maybeReverseGeocode.isPresent());
        final ReverseGeocode reverseGeocode = maybeReverseGeocode.get();

        assertTrue(reverseGeocode.results.size() >= 1, "Results list size is less than one.");
        final ReverseGeocodeResult reverseGeocodeResult = reverseGeocode.results.get(0);

        assertEquals(RADIUS, reverseGeocode.radius);
        assertEquals(DISTANCE, reverseGeocodeResult.distance);

        assertTrue(reverseGeocodeResult.names.size() >= 1, "Result names has less than one item.");
        final String resultName = reverseGeocodeResult.names.get(0);
        assertEquals(NAME, resultName);
    }

    @Test
    public void shouldCacheReverseGeocodingResponses() {
        final double latitude = 60.1;
        final double longitude = 21.3;
        final String responseTemplate = "{\"lat\":%s,\"lon\":%s,\"radius\":50,\"result\":[{\"distance\":3.1415,\"names\":[\"Lautta\"]},{\"distance\":20.24164959825527,\"names\":[\"192 - Kivimaantie\"]}]}";
        final String response = String.format(responseTemplate, latitude, longitude);
        when(this.wazeReverseGeocodingApi.fetch(anyDouble(), anyDouble())).thenReturn(Optional.of(response));

        final Optional<ReverseGeocode> maybeReverseGeocode1 = this.wazeReverseGeocodingService.fetch(latitude, longitude);
        final Optional<ReverseGeocode> maybeReverseGeocode2 = this.wazeReverseGeocodingService.fetch(latitude, longitude);

        assertNotNull(maybeReverseGeocode1);
        assertTrue(maybeReverseGeocode1.isPresent());
        assertNotNull(maybeReverseGeocode2);
        assertTrue(maybeReverseGeocode2.isPresent());

        final ReverseGeocode reverseGeocode1 = maybeReverseGeocode1.get();
        final ReverseGeocode reverseGeocode2 = maybeReverseGeocode2.get();
        assertEquals(reverseGeocode1.latitude, reverseGeocode2.latitude);
        assertEquals(reverseGeocode1.longitude, reverseGeocode2.longitude);

        verify(this.wazeReverseGeocodingApi, times(1)).fetch(latitude, longitude);
    }

    @Test
    public void shouldHandleIncorrectReverseGeocodingResults() {
        when(this.wazeReverseGeocodingApi.fetch(anyDouble(), anyDouble())).thenReturn(Optional.of(""));

        final var reverseGeocode = wazeReverseGeocodingService.fetch(60.542603, 21.336319);

        assertNotNull(reverseGeocode);
        assertTrue(reverseGeocode.isEmpty());
    }
}