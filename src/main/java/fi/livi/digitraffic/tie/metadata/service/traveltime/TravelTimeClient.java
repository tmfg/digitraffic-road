package fi.livi.digitraffic.tie.metadata.service.traveltime;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TravelTimeClient {

    private static final Logger log = LoggerFactory.getLogger(TravelTimeClient.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER =  DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private final String mediansUrl;
    private final String username;
    private final String password;

    @Autowired
    public TravelTimeClient(@Value("${traveltime.PKS.medians.url}") final String mediansUrl,
                            @Value("${traveltime.PKS.username}") final String username,
                            @Value("${traveltime.PKS.password}") final String password) {
        this.mediansUrl = mediansUrl;
        this.username = username;
        this.password = password;
    }

    public TravelTimeMediansDto getMedians(final ZonedDateTime from) {

        final RestTemplate restTemplate = getRestTemplate();

        ZonedDateTime utc = from.withZoneSameInstant(ZoneId.of("UTC"));
        String utcFormat = utc.format(DATE_TIME_FORMATTER);

        final Map parametersMap = new HashMap();
        parametersMap.put("StartTime", utcFormat);

        log.info("Fetching travel time medians from: {}", utcFormat);

        return restTemplate.postForObject(mediansUrl, parametersMap, TravelTimeMediansDto.class);
    }

    protected RestTemplate getRestTemplate() {
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        final HttpClient httpClient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(credentialsProvider)
                .build();

        final ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(requestFactory);
    }
}
