package fi.livi.digitraffic.tie.metadata.service.traveltime;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import fi.livi.digitraffic.tie.metadata.service.traveltime.dto.TravelTimeMeasurementsDto;
import fi.livi.digitraffic.tie.metadata.service.traveltime.dto.TravelTimeMediansDto;

@Service
public class TravelTimeClient {

    private static final Logger log = LoggerFactory.getLogger(TravelTimeClient.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER =  DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private final String mediansUrl;
    private final String individualMeasurementUrl;
    private final String username;
    private final String password;

    private final RestTemplate restTemplate;

    @Autowired
    public TravelTimeClient(@Value("${traveltime.PKS.medians.url}") final String mediansUrl,
                            @Value("${traveltime.PKS.individual.url}") final String individualMeasurementUrl,
                            @Value("${traveltime.PKS.username}") final String username,
                            @Value("${traveltime.PKS.password}") final String password,
                            RestTemplate restTemplate) {
        this.mediansUrl = mediansUrl;
        this.individualMeasurementUrl = individualMeasurementUrl;
        this.username = username;
        this.password = password;
        this.restTemplate = restTemplate;
    }

    public TravelTimeMediansDto getMedians(final ZonedDateTime from) {

        final String fromUtc = getDateString(from);
        log.info("Fetching travel time medians from: {}", fromUtc);

        return (TravelTimeMediansDto) getTravelTimeData(mediansUrl, fromUtc, TravelTimeMediansDto.class);
    }

    public TravelTimeMeasurementsDto getMeasurements(final ZonedDateTime from) {

        final String fromUtc = getDateString(from);
        log.info("Fetching travel time individual measurements from: {}", fromUtc);

        return (TravelTimeMeasurementsDto) getTravelTimeData(individualMeasurementUrl, fromUtc, TravelTimeMeasurementsDto.class);
    }


    public Object getTravelTimeData(final String url, final String startTime, final Class clazz) {

        HttpEntity<String> request = createRequest();

        return restTemplate.exchange(url + "?starttime=" + startTime, HttpMethod.GET, request, clazz).getBody();
    }

    private HttpEntity<String> createRequest() {
        final String plainCreds = StringUtils.join(username, ":", password);
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);

        return new HttpEntity<>(headers);
    }

    protected static String getDateString(final ZonedDateTime from) {
        final ZonedDateTime utc = from.withZoneSameInstant(ZoneId.of("UTC"));
        return utc.format(DATE_TIME_FORMATTER);
    }
}
