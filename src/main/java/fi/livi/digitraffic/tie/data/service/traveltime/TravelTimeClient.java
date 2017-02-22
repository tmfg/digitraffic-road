package fi.livi.digitraffic.tie.data.service.traveltime;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import fi.livi.digitraffic.tie.data.service.traveltime.dto.TravelTimeMeasurementsDto;
import fi.livi.digitraffic.tie.data.service.traveltime.dto.TravelTimeMediansDto;

@Service
public class TravelTimeClient {

    private static final Logger log = LoggerFactory.getLogger(TravelTimeClient.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER =  DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private final String mediansUrl;
    private final String individualMeasurementUrl;
    private final String username;
    private final String password;

    private RestTemplate restTemplate;

    @Autowired
    public TravelTimeClient(@Value("${traveltime.PKS.medians.url}") final String mediansUrl,
                            @Value("${traveltime.PKS.individual.url}") final String individualMeasurementUrl,
                            @Value("${traveltime.PKS.username}") final String username,
                            @Value("${traveltime.PKS.password}") final String password) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        this.mediansUrl = mediansUrl;
        this.individualMeasurementUrl = individualMeasurementUrl;
        this.username = username;
        this.password = password;

        final SSLConnectionSocketFactory socketFactory =
                new SSLConnectionSocketFactory(new SSLContextBuilder().loadTrustMaterial(null, (chain, authType) -> true).build());

        final HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

        final HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();

        requestFactory.setHttpClient(httpClient);
        requestFactory.setConnectTimeout(60 * 1000);
        requestFactory.setReadTimeout(60 * 1000);
        restTemplate = new RestTemplate(requestFactory);
    }

    @Retryable(backoff = @Backoff)
    public TravelTimeMediansDto getMedians(final ZonedDateTime from) {

        final String startTimeUtc = getDateString(from);
        final String url = mediansUrl + "?starttime=" + startTimeUtc;
        log.info("Fetching travel time medians from: {}", url);

        return (TravelTimeMediansDto) getTravelTimeData(url, TravelTimeMediansDto.class);
    }

    @Retryable(backoff = @Backoff)
    public TravelTimeMeasurementsDto getMeasurements(final ZonedDateTime from) {

        final String startTimeUtc = getDateString(from);
        final String url = individualMeasurementUrl + "?starttime=" + startTimeUtc;
        log.info("Fetching travel time individual measurements from: {}", url);

        return (TravelTimeMeasurementsDto) getTravelTimeData(url, TravelTimeMeasurementsDto.class);
    }

    private Object getTravelTimeData(final String url, final Class clazz) {

        final HttpEntity<String> request = createRequest();

        return restTemplate.exchange(url, HttpMethod.GET, request, clazz).getBody();
    }

    private HttpEntity<String> createRequest() {
        final String plainCreds = StringUtils.join(username, ":", password);
        final byte[] plainCredsBytes = plainCreds.getBytes();
        final byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        final String base64Creds = new String(base64CredsBytes);

        final HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);

        return new HttpEntity<>(headers);
    }

    protected static String getDateString(final ZonedDateTime from) {
        final ZonedDateTime utc = from.withZoneSameInstant(ZoneId.of("UTC"));
        return utc.format(DATE_TIME_FORMATTER);
    }
}
