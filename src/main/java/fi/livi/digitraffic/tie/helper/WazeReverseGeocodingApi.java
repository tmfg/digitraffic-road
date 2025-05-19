package fi.livi.digitraffic.tie.helper;

import java.io.IOException;
import java.util.Optional;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.common.annotation.NotTransactionalServiceMethod;

@ConditionalOnWebApplication
@Service
public class WazeReverseGeocodingApi {
    private static final Logger logger = LoggerFactory.getLogger(WazeReverseGeocodingApi.class);
    private final String token;
    private final String endpoint;

    public WazeReverseGeocodingApi(
            @Value("${waze.reverseGeocodeToken}")
            final String token,
            @Value("${waze.reverseGeocodeEndpoint}")
            final String endpoint) {
        this.token = token;
        this.endpoint = endpoint;
    }

    @NotTransactionalServiceMethod
    public Optional<String> fetch(final double latitude, final double longitude) {
        final StopWatch start = StopWatch.createStarted();
        final CloseableHttpClient httpClient = HttpClients.createDefault();
        final String url = String.format("%s?lat=%s&lon=%s&token=%s", endpoint, latitude, longitude, token);

        final HttpGet httpGet = new HttpGet(url);
        final ResponseHandler<Optional<String>> responseHandler = createResponseHandler();

        try {
            return httpClient.execute(httpGet, responseHandler);
        } catch (final IOException e) {
            logger.error("method=fetch Unable to fetch data from waze reverse geocode api", e);
        } finally {
            logger.info(
                    "method=fetch reverse geocoding for lat: {} and lon: {} tookMs={}",
                    latitude, longitude,
                    start.getDuration().toMillis());
        }
        return Optional.empty();
    }

    private ResponseHandler<Optional<String>> createResponseHandler() {
        return httpResponse -> {
            final int responseCode = httpResponse.getStatusLine().getStatusCode();

            if (200 <= responseCode && responseCode < 300) {
                final HttpEntity httpEntity = httpResponse.getEntity();
                return httpEntity != null ? Optional.of(EntityUtils.toString(httpEntity)) : Optional.empty();
            }

            logger.error(String.format("method=fetch status=%s api returned incorrect status code", responseCode));
            return Optional.empty();
        };
    }
}
