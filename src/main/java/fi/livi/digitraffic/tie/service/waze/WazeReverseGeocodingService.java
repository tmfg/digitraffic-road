package fi.livi.digitraffic.tie.service.waze;

import static fi.livi.digitraffic.tie.conf.RoadCacheConfiguration.CACHE_REVERSE_GEOCODE;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import fi.livi.digitraffic.common.annotation.NotTransactionalServiceMethod;
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

    @Autowired
    public WazeReverseGeocodingService(final ObjectMapper objectMapper,
                                       final WazeReverseGeocodingApi wazeReverseGeocodingApi) {
        this.genericJsonReader = objectMapper.reader();
        this.wazeReverseGeocodingApi = wazeReverseGeocodingApi;
    }

    @Cacheable(value = CACHE_REVERSE_GEOCODE)
    @NotTransactionalServiceMethod
    public Optional<String> getStreetName(final Geometry<?> geometry) {
        return getPoint(geometry)
                .flatMap(this::fetch)
                .flatMap(this::closestStreetName);
    }

    @CacheEvict(value = CACHE_REVERSE_GEOCODE,
                allEntries = true)
    @NotTransactionalServiceMethod
    public void evictCache() {
    }

    private Optional<Point> getPoint(final Geometry<?> geometry) {
        if (geometry instanceof Point) {
            return Optional.of((Point) geometry);
        } else if (geometry instanceof MultiLineString) {
            // get first linestring and middle coordinates
            return ((MultiLineString) geometry).getCoordinates().stream()
                    .findFirst()
                    .flatMap(this::middleElement)
                    .flatMap(pair -> Optional.of(new Point(pair.getFirst(), pair.get(1))));
        }

        logger.warn(String.format("method=getPoint Unknown geometry type %s", geometry.getClass().getSimpleName()));
        return Optional.empty();
    }

    private <T> Optional<T> middleElement(final List<T> list) {
        if (list.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(list.get(list.size() / 2));
    }

    private Optional<String> closestStreetName(final ReverseGeocode reverseGeocode) {
        return reverseGeocode.results.stream()
                .reduce((accumulator, element) -> accumulator.distance > element.distance ? element : accumulator)
                .flatMap(reverseGeocodeResult -> reverseGeocodeResult.names.stream().findFirst());
    }

    private Optional<ReverseGeocode> fetch(final Point point) {
        final Double latitude = point.getLatitude();
        final Double longitude = point.getLongitude();

        return wazeReverseGeocodingApi
                .fetch(latitude, longitude)
                .flatMap(this::parseReverseGeocodeJson)
                .or(() -> {
                    logger.info(String.format(Locale.US, "method=fetch empty response for lat: %f, lon: %f", latitude,
                            longitude));

                    return Optional.empty();
                });
    }

    private Optional<ReverseGeocode> parseReverseGeocodeJson(final String input) {
        try {
            final ReverseGeocode reverseGeocode = this.genericJsonReader.readValue(input, ReverseGeocode.class);
            return Optional.of(reverseGeocode);
        } catch (final IOException e) {
            logger.error("method=parseReverseGeocodeJson unable to parse input.", e);
        }
        return Optional.empty();
    }
}
