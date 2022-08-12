package fi.livi.digitraffic.tie.service;

import java.io.IOException;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import org.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.dto.wazefeed.ReverseGeocode;
import fi.livi.digitraffic.tie.dto.wazefeed.ReverseGeocodeResult;
import fi.livi.digitraffic.tie.helper.RoadCacheHelper;
import fi.livi.digitraffic.tie.helper.WazeReverseGeocodingApi;

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

    public Optional<ReverseGeocode> fetch(final double latitude, final double longitude) {
        final String cacheKey = String.format("%f,%f", latitude, longitude);

        if (cache.containsKey(cacheKey)) {
            logger.info(String.format("Retrieve reverse geocoding for lat: %f, lon: %f from cache", latitude, longitude));
            return Optional.of(cache.get(cacheKey));
        }

        logger.info(String.format("Get reverse geocoding for lat: %f, lon: %f", latitude, longitude));
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