package fi.livi.digitraffic.tie.service.v1.lotju;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import fi.livi.digitraffic.tie.AbstractDaemonTestWithoutS3;

public abstract class AbstractMultiDestinationProviderTest extends AbstractDaemonTestWithoutS3 {

    private static final Logger log = LoggerFactory.getLogger(AbstractMultiDestinationProviderTest.class);

    public static final int RANDOM_PORT1 = (int)RandomUtils.nextLong(6000,6500);
    public static final int RANDOM_PORT2 = (int)RandomUtils.nextLong(6500,7000);

    protected final static String baseUrl1 = "http://localhost:" + RANDOM_PORT1;
    protected final static String baseUrl2 = "http://localhost:" + RANDOM_PORT2;
    protected final static String[] baseUrls = { baseUrl1, baseUrl2 };
    protected final static String healthPath = "/health";
    protected final static String dataPath = "/data";

    protected final static String dataUrl1 = baseUrl1 + dataPath;
    protected final static String dataUrl2 = baseUrl2 + dataPath;

    protected final static String OK_CONTENT = "ok!";
    protected final static String NOT_OK_CONTENT = "eok";

    protected final static int TTL_S = 1;

    @Rule
    public WireMockRule wireMockRule1 = new WireMockRule(wireMockConfig().port(RANDOM_PORT1), true);

    @Rule
    public WireMockRule wireMockRule2 = new WireMockRule(wireMockConfig().port(RANDOM_PORT2), true);

    protected MultiDestinationProvider createMultiDestinationProvider() {
        return new MultiDestinationProvider(AbstractLotjuMetadataClient.createHostsWithHealthCheck(baseUrls, dataPath, healthPath, TTL_S));
    }

    protected MultiDestinationProvider createMultiDestinationProviderWithoutHealthCheck() {
        return new MultiDestinationProvider(AbstractLotjuMetadataClient.createHostsWithHealthCheck(baseUrls, dataPath, null, TTL_S));
    }


    protected void server1WhenRequestHealthThenReturn(final HttpStatus returnStatus, final String returnContent) {
        serverWhenRequestUrlThenReturn(wireMockRule1, healthPath, returnStatus, returnContent);
    }

    protected void server2WhenRequestHealthThenReturn(final HttpStatus returnStatus, final String returnContent) {
        serverWhenRequestUrlThenReturn(wireMockRule2, healthPath, returnStatus, returnContent);
    }

    protected void serverWhenRequestUrlThenReturn(final WireMockRule wireMockRule, final String expectedUrl, final HttpStatus returnStatus, final String returnContent) {
        log.info("Register url {} to return {} : {}", expectedUrl, returnStatus, returnContent);
        wireMockRule.givenThat(
            get(urlEqualTo(expectedUrl))
                .willReturn(aResponse()
                .withBody(returnContent)
                .withHeader("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                .withStatus(returnStatus.value())));
    }

    protected void verifyServer1HealthCount(final int count) {
        assertEquals(count, wireMockRule1.getServeEvents().getRequests().size());
    }

    protected void verifyServer2HealthCount(final int count) {
        assertEquals(count, wireMockRule2.getServeEvents().getRequests().size());
    }
}
