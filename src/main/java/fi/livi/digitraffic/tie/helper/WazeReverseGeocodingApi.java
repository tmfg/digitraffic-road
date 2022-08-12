package fi.livi.digitraffic.tie.helper;

import java.io.IOException;
import java.util.Optional;

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
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@ConditionalOnWebApplication
@Component
@Configuration
public class WazeReverseGeocodingApi {
    private static final Logger logger = LoggerFactory.getLogger(WazeReverseGeocodingApi.class);
    private final String token;
    private final String endpoint;

    public WazeReverseGeocodingApi(@Value("${waze.reverseGeocodeToken}") final String token, @Value("${waze.reverseGeocodeEndpoint") final String endpoint) {
        this.token = token;
        this.endpoint = endpoint;
    }

    public Optional<String> fetch(double latitude, double longitude) {
        final CloseableHttpClient httpClient = HttpClients.createDefault();
        final String url = String.format("%s?lat=%s&lon=%s&token=%s", endpoint, latitude, longitude, token);

        final HttpGet httpGet = new HttpGet(url);
        final ResponseHandler<Optional<String>> responseHandler = createResponseHandler();

        try {
            return httpClient.execute(httpGet, responseHandler);
        } catch (IOException e) {
            logger.error("method=fetch Unable to fetch data from waze reverse geocode api", e);
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
            return Optional.empty();
        };
    }
}