package fi.livi.digitraffic.tie.service.v1.lotju;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.spy;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

import javax.xml.bind.JAXBElement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.oxm.MarshallingFailureException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceMessageCallback;

import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.HaeKaikkiTiesaaAsemat;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.ObjectFactory;
import fi.livi.digitraffic.tie.service.v1.lotju.AbstractLotjuMetadataClient.WebServiceTemplateWithMultiDestinationProviderSupport;

public class WebServiceTemplateWithMultiDestinationProviderTest extends AbstractMultiDestinationProviderTest {

    final static String RESPONSE1 = "RESPONSE1";
    final static String RESPONSE2 = "RESPONSE2";
    final ObjectFactory objectFactory = new ObjectFactory();
    private AbstractLotjuMetadataClient client;
    private WebServiceTemplateWithMultiDestinationProviderSupport webServiceTemplate;

    @BeforeEach
    public void initSoapClientSpyAndServerResponses() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        client = new AbstractLotjuMetadataClient(marshaller, baseUrls, dataPath, healthPath, TTL_S, healthOkCheckValueInApplicationSettings) {};
        // Get get WebServiceTemplate, spy it and set spy to client
        webServiceTemplate = (WebServiceTemplateWithMultiDestinationProviderSupport) spy(client.getWebServiceTemplate());
        client.setWebServiceTemplate(webServiceTemplate);

        // SOAP servers 1 & 2 return always same values
        server1WhenRequestDataThenReturn(RESPONSE1);
        server2WhenRequestDataThenReturn(RESPONSE2);
    }

    /*
     * There is always 1. health request and if it's ok then request to client
     */
    @Test
    public void firstHealthOk() {
        // Health response from server OK
        server1WhenRequestHealthThenReturn(OK, getOkResponseString());
        // Data request goes to server 1
        clientRequestDataAndVerifyResponse(RESPONSE1);
        verifyServer1HealthCount(1);
        verifyServer2HealthCount(0);
    }

    @Test
    public void firstHealthNotOkSecondOk() {
        server1WhenRequestHealthThenReturn(OK, NOT_OK_RESPONSE_CONTENT);
        server2WhenRequestHealthThenReturn(OK, getOkResponseString());
        // Data request goes to server 2
        clientRequestDataAndVerifyResponse(RESPONSE2);
        verifyServer1HealthCount(1);
        verifyServer2HealthCount(1);
    }

    @Test
    public void firstHealthErrorSecondOk() {
        server1WhenRequestHealthThenReturn(BAD_REQUEST, null);
        server2WhenRequestHealthThenReturn(OK, getOkResponseString());
        // Data request goes to server 2
        clientRequestDataAndVerifyResponse(RESPONSE2);
        verifyServer1HealthCount(1);
        verifyServer2HealthCount(1);
    }

    @Test
    public void firstAndSecondHealthNotOk() {
        server1WhenRequestHealthThenReturn(BAD_REQUEST, null);
        server2WhenRequestHealthThenReturn(OK, NOT_OK_RESPONSE_CONTENT);

        // Exception should be thrown
        try {
            clientRequestData();
        } catch (IllegalStateException e) {
            verifyServer1HealthCount(1);
            verifyServer2HealthCount(1);
            throw e;
        }
        fail("Should not execute as IllegalStateException should have been thrown");
    }

    @Test
    public void healthRequestOnlyOnceAsTTLNotPassed() {
        server1WhenRequestHealthThenReturn(OK, getOkResponseString());

        // first request: health + data requests to server
        // second request: health from cache and data request to server
        // Data request goes to server 1 both times
        clientRequestDataAndVerifyResponse(RESPONSE1); // also health request
        clientRequestDataAndVerifyResponse(RESPONSE1); // no health request
        verifyServer1HealthCount(1);
        verifyServer2HealthCount(0);
    }

    @Test
    public void healthRequestTwiceAsTTLPassed() {
        server1WhenRequestHealthThenReturn(OK, getOkResponseString());

        // Data request goes to server 1 both times
        clientRequestDataAndVerifyResponse(RESPONSE1); // also health request
        try {
            Thread.sleep(1100L);
        } catch (InterruptedException e) {
            // empty
        }
        clientRequestDataAndVerifyResponse(RESPONSE1); // also health request
        verifyServer1HealthCount(2);
        verifyServer2HealthCount(0);
    }

    @Test
    public void healthOkButDataOnServer1Fails() {
        server1WhenRequestHealthThenReturn(OK, getOkResponseString());
        server2WhenRequestHealthThenReturn(OK, getOkResponseString());

        // Reset server 1 to return error on data query
        Mockito.reset(webServiceTemplate);
        server1WhenRequestDataThenThrowException();
        server2WhenRequestDataThenReturn(RESPONSE2);

        // Health 1 -> ok, data 1 -> fail, health 2 -> ok, data 2 ok
        clientRequestDataAndVerifyResponse(RESPONSE2); // also health request
        verifyServer1HealthCount(1);
        verifyServer2HealthCount(1);
    }

    @Test
    public void hostWithoutHealthCheck() {
        initClientWithoutHealthCheck();

        clientRequestDataAndVerifyResponse(RESPONSE1); // No health requests
        verifyServer1HealthCount(0);
        verifyServer2HealthCount(0);
    }

    @Test
    public void host1WithoutHealthCheckButUnhealthy() {
        initClientWithoutHealthCheck();

        // Reset server 1 to return error on data query
        Mockito.reset(webServiceTemplate);
        server1WhenRequestDataThenThrowException();
        server2WhenRequestDataThenReturn(RESPONSE2);

        // Ddata 1 -> fail, data 2 ok
        clientRequestDataAndVerifyResponse(RESPONSE2); // No health requests
        verifyServer1HealthCount(0);
        verifyServer2HealthCount(0);
    }

    private void initClientWithoutHealthCheck() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        client = new AbstractLotjuMetadataClient(marshaller, baseUrls, dataPath, TTL_S) {};
        // Get get WebServiceTemplate, spy it and set spy to client
        webServiceTemplate = (WebServiceTemplateWithMultiDestinationProviderSupport) spy(client.getWebServiceTemplate());
        client.setWebServiceTemplate(webServiceTemplate);

        // SOAP servers 1 & 2 return always same values
        server1WhenRequestDataThenReturn(RESPONSE1);
        server2WhenRequestDataThenReturn(RESPONSE2);
    }

    private void clientRequestDataAndVerifyResponse(String response) {
        assertEquals(response, clientRequestData());
    }

    private Object clientRequestData() {
        return client.marshalSendAndReceive(objectFactory.createHaeKaikkiTiesaaAsemat(new HaeKaikkiTiesaaAsemat()));
    }

    private void server1WhenRequestDataThenReturn(final String response) {
        serverWhenRequestUrlThenReturn(dataUrl1, response);
    }

    private void server2WhenRequestDataThenReturn(final String response) {
        serverWhenRequestUrlThenReturn(dataUrl2, response);
    }

    private void serverWhenRequestUrlThenReturn(final String requestUrl, final String response) {
        doReturn(response).when(webServiceTemplate).marshalSendAndReceive(eq(requestUrl), nullable(JAXBElement.class), nullable(WebServiceMessageCallback.class));
    }

    private void server1WhenRequestDataThenThrowException() {
        serverWhenRequestDataThenThrowException(dataUrl1);
    }

    private void serverWhenRequestDataThenThrowException(final String requestUrl) {
        doThrow(new MarshallingFailureException("JAXB marshalling exception")).when(webServiceTemplate).marshalSendAndReceive(eq(dataUrl1), nullable(JAXBElement.class), nullable(WebServiceMessageCallback.class));
    }

}
