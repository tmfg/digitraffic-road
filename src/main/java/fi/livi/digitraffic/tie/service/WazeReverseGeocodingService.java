package fi.livi.digitraffic.tie.service;

import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import org.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.dto.wazefeed.ReverseGeocode;
import fi.livi.digitraffic.tie.dto.wazefeed.ReverseGeocodeResult;
import fi.livi.digitraffic.tie.helper.RoadCacheHelper;
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

    private final Cache<String, ReverseGeocode> cache;

    @Autowired
    public WazeReverseGeocodingService(final ObjectMapper objectMapper, final WazeReverseGeocodingApi wazeReverseGeocodingApi, final RoadCacheHelper roadCacheHelper) {
        this.genericJsonReader = objectMapper.reader();
        this.wazeReverseGeocodingApi = wazeReverseGeocodingApi;
        this.cache = roadCacheHelper.getWazeReverseGeocodeCache();
    }

    public Optional<String> getStreetName(final Geometry<?> geometry) {
        return getPoint(geometry)
            .flatMap(this::fetch)
            .flatMap(this::closestStreetName);
    }

    private Optional<Point> getPoint(final Geometry<?> geometry) {
        if (geometry instanceof Point) {
            return Optional.of((Point) geometry);
        } else if (geometry instanceof MultiLineString) {
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

    public Optional<ReverseGeocode> fetch(final Point point) {
        return fetch(point.getLatitude(), point.getLongitude());
    }

    public Optional<ReverseGeocode> fetch(final double latitude, final double longitude) {
        final String cacheKey = String.format(Locale.US, "%f,%f", latitude, longitude);

        if (cache.containsKey(cacheKey)) {
            logger.info(String.format(Locale.US, "Retrieve reverse geocoding for lat: %f, lon: %f from cache", latitude, longitude));
            return Optional.of(cache.get(cacheKey));
        }

        logger.info(String.format(Locale.US, "Get reverse geocoding for lat: %f, lon: %f", latitude, longitude));
        return wazeReverseGeocodingApi
            .fetch(latitude, longitude)
            .flatMap(this::parseReverseGeocodeJson)
            .map(r -> {
                cache.put(cacheKey, r);
                return r;
            });
    }

    public Optional<ReverseGeocode> parseReverseGeocodeJson(final String input) {
        try {
            ReverseGeocode reverseGeocode = this.genericJsonReader.readValue(input, ReverseGeocode.class);
            return Optional.of(reverseGeocode);
        } catch (IOException e) {
            logger.error("method=parseReverseGeocodeJson unable to parse input.", e);
        }
        return Optional.empty();
    }
}