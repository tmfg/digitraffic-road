package fi.livi.digitraffic.tie.service.v1.lotju;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

import java.net.URI;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceMessageCallback;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import fi.livi.digitraffic.tie.AbstractDaemonTestWithoutS3;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.HaeKaikkiTiesaaAsemat;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.ObjectFactory;
import fi.livi.digitraffic.tie.service.v1.lotju.AbstractLotjuMetadataClient.WebServiceTemplateWithMultiDestinationProviderSupport;

public class MultiDestinationProviderTest extends AbstractDaemonTestWithoutS3 {

    private static final Logger log = LoggerFactory.getLogger(MultiDestinationProviderTest.class);

    public static final int RANDOM_PORT1 = (int)RandomUtils.nextLong(6000,6500);
    public static final int RANDOM_PORT2 = (int)RandomUtils.nextLong(6500,7000);
    private final static String baseUrl1 = "http://localhost:" + RANDOM_PORT1;
    private final static String baseUrl2 = "http://localhost:" + RANDOM_PORT2;
    private final static String baseUrls = baseUrl1 + "," + baseUrl2;
    private final static String healthPath = "/health";
    private final static String dataPath = "/data";

    private final static String dataUrl1 = baseUrl1 + dataPath;
    private final static String dataUrl2 = baseUrl2 + dataPath;

    private final static String OK_CONTENT = "ok!";
    private final static String NOT_OK_CONTENT = "eok";

    private final static int TTL_S = 1;

    @Rule
    public WireMockRule wireMockRule1 = new WireMockRule(wireMockConfig().port(RANDOM_PORT1));
    @Rule
    public WireMockRule wireMockRule2 = new WireMockRule(wireMockConfig().port(RANDOM_PORT2));

    final ObjectFactory objectFactory = new ObjectFactory();
    private AbstractLotjuMetadataClient client;
    private WebServiceTemplateWithMultiDestinationProviderSupport webServiceTemplate;

    @Before
    public void initClient() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        client = new AbstractLotjuMetadataClient(marshaller, baseUrls, healthPath, dataPath, TTL_S, log);
        webServiceTemplate = (WebServiceTemplateWithMultiDestinationProviderSupport) spy(client.getWebServiceTemplate());
        client.setWebServiceTemplate(webServiceTemplate);
    }

    @Test
    public void firstHealtOk() {
        final MultiDestinationProvider mdp = createMultiDestinationProvider();
        server1Expect(healthPath, OK, OK_CONTENT);
        final URI dest = mdp.getDestination();
        Assert.assertEquals(dataUrl1, dest.toString());
    }

    @Test
    public void firstHealtOkWithClient() {
        server1Expect(healthPath, OK, OK_CONTENT);
//        doReturn("TEST1").when(webServiceTemplate).marshalSendAndReceive(ArgumentMatchers.same(dataUrl1), nullable(JAXBElement.class), nullable(WebServiceMessageCallback.class));
//        doReturn("TEST2").when(webServiceTemplate).marshalSendAndReceive(ArgumentMatchers.same(dataUrl1), any(JAXBElement.class), any(WebServiceMessageCallback.class));
        doReturn("TEST3").when(webServiceTemplate).marshalSendAndReceive(anyString(), nullable(JAXBElement.class), nullable(WebServiceMessageCallback.class));
//        doReturn("TEST4").when(webServiceTemplate).marshalSendAndReceive(anyString(), any(), any());
        Object r = clientRequestData();
        log.info(r.toString());
    }

    @Test
    public void firstHealtNotOkSecondOk() {
        final MultiDestinationProvider mdp = createMultiDestinationProvider();
        server1Expect(healthPath, OK, NOT_OK_CONTENT);
        server2Expect(healthPath, OK, OK_CONTENT);
        final URI dest = mdp.getDestination();
        Assert.assertEquals(dataUrl2, dest.toString());
    }

    @Test
    public void firstHealtErrorSecondOk() {
        final MultiDestinationProvider mdp = createMultiDestinationProvider();
        server1Expect(healthPath, BAD_REQUEST, null);
        server2Expect(healthPath, OK, OK_CONTENT);
        final URI dest = mdp.getDestination();
        Assert.assertEquals(dataUrl2, dest.toString());
    }

    @Test(expected = IllegalStateException.class)
    public void firstAndSecondHealtNotOk() {
        final MultiDestinationProvider mdp = createMultiDestinationProvider();
        server1Expect(healthPath, BAD_REQUEST, null);
        server2Expect(healthPath, OK, NOT_OK_CONTENT);
        mdp.getDestination();
        Assert.fail("Should not execute as IllegalStateException should have been thrown");
    }

    @Test
    public void healthRequestOnlyOnceAsTTLNotPassed() {
        final MultiDestinationProvider mdp = createMultiDestinationProvider();
        server1Expect(healthPath, OK, OK_CONTENT);
        final URI dest1 = mdp.getDestination();
        Assert.assertEquals(dataUrl1, dest1.toString());
        final URI dest2 = mdp.getDestination();
        Assert.assertEquals(dataUrl1, dest2.toString());
    }

    @Test
    public void healthRequestTwiceAsTTLPassed() {
        final MultiDestinationProvider mdp = createMultiDestinationProvider();
        server1Expect(healthPath, OK, OK_CONTENT);
        server1Expect(healthPath, OK, OK_CONTENT);
        final URI dest1 = mdp.getDestination();
        Assert.assertEquals(dataUrl1, dest1.toString());
        try {
            Thread.sleep(1100L);
        } catch (InterruptedException e) {
            // empty
        }
        final URI dest2 = mdp.getDestination();
        Assert.assertEquals(dataUrl1, dest2.toString());
    }

    private MultiDestinationProvider createMultiDestinationProvider() {
        return new MultiDestinationProvider(baseUrls, healthPath, dataPath, TTL_S);
    }

    private void server1Expect(final String expectedUrl, final HttpStatus returnStatus, final String returnContent) {
        serverExpect(wireMockRule1, expectedUrl, returnStatus, returnContent);
    }

    private void server2Expect(final String expectedUrl, final HttpStatus returnStatus, final String returnContent) {
        serverExpect(wireMockRule2, expectedUrl, returnStatus, returnContent);
    }

    private void serverExpect(final WireMockRule wireMockRule, final String expectedUrl, final HttpStatus returnStatus, final String returnContent) {
        log.info("Register url {} to return {} : {}", expectedUrl, returnStatus, returnContent);
        wireMockRule.givenThat(get(urlEqualTo(expectedUrl))
            .willReturn(aResponse()
                .withBody(returnContent)
                .withHeader("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                .withStatus(returnStatus.value())));
    }

    private Object clientRequestData() {
        return client.marshalSendAndReceive(objectFactory.createHaeKaikkiTiesaaAsemat(new HaeKaikkiTiesaaAsemat()));
    }
}
