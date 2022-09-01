package fi.livi.digitraffic.tie.service;

import static fi.livi.digitraffic.tie.conf.RoadCacheConfiguration.CACHE_REVERSE_GEOCODE;

import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dto.wazefeed.ReverseGeocode;
import fi.livi.digitraffic.tie.helper.WazeReverseGeocodingApi;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.metadata.geojson.MultiLineString;
import fi.livi.digitraffic.tie.metadata.geojson.Point;

@ConditionalOnWebApplication
@Service
public class WazeReverseGeocodingService {
    private static final Logger logger = LoggerFactory.getLogger(WazeReverseGeocodingService.class);

    private final ObjectReader genericJsonReader;

    private final WazeReverseGeocodingApi wazeReverseGeocodingApi;

    private final boolean featureFlagMultiLineString;

    @Autowired
    public WazeReverseGeocodingService(@Value("${waze.feature.multilinestring: true}") final boolean featureFlagMultiLineString, final ObjectMapper objectMapper, final WazeReverseGeocodingApi wazeReverseGeocodingApi) {
        this.featureFlagMultiLineString = featureFlagMultiLineString;
        this.genericJsonReader = objectMapper.reader();
        this.wazeReverseGeocodingApi = wazeReverseGeocodingApi;
    }

    @Cacheable(CACHE_REVERSE_GEOCODE)
    @Transactional(readOnly = true)
    public Optional<String> getStreetName(final Geometry<?> geometry) {
        return getPoint(geometry)
            .flatMap(this::fetch)
            .flatMap(this::closestStreetName);
    }

    @CacheEvict(value = CACHE_REVERSE_GEOCODE, allEntries = true)
    @Transactional(readOnly = true)
    public void evictCache () { }

    private Optional<Point> getPoint(final Geometry<?> geometry) {
        if (geometry instanceof Point) {
            return Optional.of((Point) geometry);
        } else if (featureFlagMultiLineString && geometry instanceof MultiLineString) {
            return ((MultiLineString) geometry).getCoordinates().stream()
                .flatMap(Collection::stream)
                .findFirst()
                .map(pair -> new Point(pair.get(0), pair.get(1)));
        }

        logger.warn(String.format("method=getPoint Unknown geometry type %s", geometry.getClass().getSimpleName()));
        return Optional.empty();
    }

    private Optional<String> closestStreetName(final ReverseGeocode reverseGeocode) {
        return reverseGeocode.results.stream()
            .reduce((accumulator, element) -> accumulator.distance > element.distance ? element : accumulator)
            .flatMap(reverseGeocodeResult -> reverseGeocodeResult.names.stream().findFirst());
    }

    private Optional<ReverseGeocode> fetch(final Point point) {
        final Double latitude = point.getLatitude();
        final Double longitude = point.getLongitude();

        logger.info(String.format(Locale.US, "Get reverse geocoding for lat: %f, lon: %f", latitude, longitude));
        return wazeReverseGeocodingApi
            .fetch(latitude, longitude)
            .flatMap(this::parseReverseGeocodeJson);
    }

    private Optional<ReverseGeocode> parseReverseGeocodeJson(final String input) {
        try {
            ReverseGeocode reverseGeocode = this.genericJsonReader.readValue(input, ReverseGeocode.class);
            return Optional.of(reverseGeocode);
        } catch (IOException e) {
            logger.error("method=parseReverseGeocodeJson unable to parse input.", e);
        }
        return Optional.empty();
    }
}