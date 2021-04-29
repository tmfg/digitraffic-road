package fi.livi.digitraffic.tie.service.v1.lotju;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import fi.livi.digitraffic.tie.AbstractDaemonTestWithouLocalStack;

public abstract class AbstractMultiDestinationProviderTest extends AbstractDaemonTestWithouLocalStack {
    private static final Logger log = LoggerFactory.getLogger(AbstractMultiDestinationProviderTest.class);

    public static final int RANDOM_PORT1 = (int)RandomUtils.nextLong(6000,6500);
    public static final int RANDOM_PORT2 = (int)RandomUtils.nextLong(6500,7000);

    protected final static String baseUrl1 = "http://localhost:" + RANDOM_PORT1;
    protected final static String baseUrl2 = "http://localhost:" + RANDOM_PORT2;
    protected final static String[] baseUrls = { baseUrl1, baseUrl2 };
    protected final static String healthPath = "/health";
    protected final static String dataPath = "/data";
    protected final static String healthOkCheckValueInApplicationSettings = "ok";

    protected final static String dataUrl1 = baseUrl1 + dataPath;
    protected final static String dataUrl2 = baseUrl2 + dataPath;

    private final static String OK_RESPONSE_CONTENT = "ok";
    protected final static String NOT_OK_RESPONSE_CONTENT = "eok";

    protected final static int TTL_S = 1;

    protected WireMockServer wireMockServer1 = new WireMockServer(options().port(RANDOM_PORT1));
    protected WireMockServer wireMockServer2 = new WireMockServer(options().port(RANDOM_PORT2));

    @AfterEach
    public void closeServers() {
        if(wireMockServer1.isRunning()) wireMockServer1.stop();
        if(wireMockServer2.isRunning()) wireMockServer2.stop();
    }

    protected MultiDestinationProvider createMultiDestinationProvider() {
        return new MultiDestinationProvider(HostWithHealthCheck.createHostsWithHealthCheck(baseUrls, dataPath, healthPath, TTL_S,
            healthOkCheckValueInApplicationSettings));
    }

    protected MultiDestinationProvider createMultiDestinationProviderWithoutHealthCheck() {
        return new MultiDestinationProvider(HostWithHealthCheck.createHostsWithHealthCheck(baseUrls, dataPath, null, TTL_S, null));
    }


    protected void server1WhenRequestHealthThenReturn(final HttpStatus returnStatus, final String returnContent) {
        serverWhenRequestUrlThenReturn(wireMockServer1, healthPath, returnStatus, returnContent);
    }

    protected void server2WhenRequestHealthThenReturn(final HttpStatus returnStatus, final String returnContent) {
        serverWhenRequestUrlThenReturn(wireMockServer2, healthPath, returnStatus, returnContent);
    }

    protected void serverWhenRequestUrlThenReturn(final WireMockServer server, final String expectedUrl, final HttpStatus returnStatus, final String returnContent) {
        log.info("Register url {} to return {} : {}", expectedUrl, returnStatus, returnContent);

        server.start();

        server.givenThat(
            get(urlEqualTo(expectedUrl))
                .willReturn(aResponse()
                .withBody(returnContent)
                .withHeader(CONTENT_TYPE, TEXT_PLAIN_VALUE)
                .withStatus(returnStatus.value())));
    }

    protected void serverWhenRequestUrlThenReturn(final WireMockServer server, final String expectedUrl, final HttpStatus returnStatus, final byte[] returnContent) {
        log.info("Register url {} to return {} : {}", expectedUrl, returnStatus, returnContent);

        server.start();

        server.givenThat(
            get(urlEqualTo(expectedUrl))
                .willReturn(aResponse()
                    .withBody(returnContent)
                    .withHeader(CONTENT_TYPE, IMAGE_JPEG_VALUE)
                    .withStatus(returnStatus.value())));
    }

    protected void verifyServer1HealthCount(final int count) {
        verifyServerCalledCount(count, healthPath, wireMockServer1);
    }

    protected void verifyServer2HealthCount(final int count) {
        verifyServerCalledCount(count, healthPath, wireMockServer2);
    }

    protected void verifyServer1DataCount(final int count) {
        verifyServerCalledCount(count, dataPath, wireMockServer1);
    }

    protected void verifyServer2DataCount(final int count) {
        verifyServerCalledCount(count, dataPath, wireMockServer2);
    }

    protected void verifyServerCalledCount(final int count, final String pathPrefix, final WireMockServer server) {
        final int loggedCount = (int) server.getAllServeEvents().stream().filter(e -> e.getRequest().getUrl().startsWith(pathPrefix)).count();
        assertEquals(count, loggedCount);
    }

    /**
     * Returns ok and adds randomly extra string after that
     * @return
     */
    public static String getOkResponseString() {
        final String value = OK_RESPONSE_CONTENT + RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(0, 2));
        log.info("getOkResponseString {}", value);
        return value;
    }
}
