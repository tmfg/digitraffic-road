package fi.livi.digitraffic.tie.service.v1.forecastsection;

import static fi.livi.digitraffic.tie.TestUtils.loadResource;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ForecastSectionTestHelper {
    private static final Logger log = LoggerFactory.getLogger(ForecastSectionTestHelper.class);

    @Value("${roadConditions.baseUrl}")
    private String baseUrl;

    @Value("${roadConditions.suid}")
    private String suid;

    @Value("${roadConditions.user}")
    private String user;

    @Value("${roadConditions.pass}")
    private String pass;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public ForecastSectionClient createForecastSectionClient() {
        return new ForecastSectionClient(restTemplate, objectMapper, baseUrl, suid, user, pass);
    }

    public void serverExpectMetadata(final MockRestServiceServer server, final int version) {
        serverExpect(server, version, true);
    }

    public void serverExpectData(final MockRestServiceServer server, final int version) {
        serverExpect(server, version, false);
    }

    private void serverExpect(final MockRestServiceServer server, final int version, final boolean metadata) {
        final String url = getUrl(version, metadata);
        final String resourcePattern = getResourcePattern(version, metadata);
        log.info("serverExpect {}", url);
        log.info("serverReturn {}", resourcePattern);
        server.expect(requestTo(url)).andExpect(method(HttpMethod.GET))
              .andExpect(method(HttpMethod.GET))
              .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE))
              .andRespond(MockRestResponseCreators.withSuccess(loadResource(resourcePattern), MediaType.APPLICATION_OCTET_STREAM));
    }

    private String getResourcePattern(final int version, final boolean metadata) {
        final String prefix = "classpath:forecastsection/";
        if (metadata) {
            if (version == 1) {
                return prefix + "roadsV1.json.gz";
            } else if (version == 2) {
                return prefix + "roadsV2.json.gz";
            } else if (version == 3) {
                return prefix + "roadsV3.json.gz";
            }
        } else {
            if (version == 1) {
                return prefix + "keliennuste-v1.json.gz";
            } else if (version == 2) {
                return prefix + "keliennuste-v2.json.gz";
            } else if (version == 3) {
                return prefix + "keliennuste-v3.json.gz";
            }
        }
        throw new IllegalArgumentException("Unsuported version " + version);
    }

    public String getUrl(final int version, final boolean metadata) {
        if (metadata) {
            if (version == 1) {
                return baseUrl + "roadsV1.json.gz?suid=" + suid + "&user=" + user + "&pass=" + pass;
            } else if (version == 2) {
                return baseUrl + "roadsV2.json.gz?suid=" + suid + "&user=" + user + "&pass=" + pass;
            } else if (version == 3) {
                return baseUrl + "roadsV3.json.gz?suid=" + suid + "&user=" + user + "&pass=" + pass;
            }
        } else {
            if (version == 1) {
                return baseUrl + "json/keliennuste.json.gz?suid=" + suid + "&user=" + user + "&pass=" + pass;
            } else if (version == 2) {
                return baseUrl + "keliennuste-v2.json.gz?suid=" + suid + "&user=" + user + "&pass=" + pass;
            } else if (version == 3) {
                return baseUrl + "keliennuste-v3.json.gz?suid=" + suid + "&user=" + user + "&pass=" + pass;
            }
        }
        throw new IllegalArgumentException("Unsuported version " + version);
    }
}
