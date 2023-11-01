package fi.livi.digitraffic.tie.service.lotju;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

import java.net.URI;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import fi.livi.digitraffic.tie.helper.ThreadUtils;

public class MultiDestinationProviderTest extends AbstractMultiDestinationProviderTest {

    @Test
    public void firstHealthOk() {
        final MultiDestinationProvider mdp = createMultiDestinationProvider();
        server1WhenRequestHealthThenReturn(OK, getOkResponseString());
        final URI dest = mdp.getDestination();

        assertEquals(dataUrl1, dest.toString());
        verifyServer1HealthCount(1);
        verifyServer2HealthCount(0);
    }

    @Test
    public void firstHealthNotOkSecondOk() {
        final MultiDestinationProvider mdp = createMultiDestinationProvider();
        server1WhenRequestHealthThenReturn(OK, NOT_OK_RESPONSE_CONTENT);
        server2WhenRequestHealthThenReturn(OK, getOkResponseString());
        final URI dest = mdp.getDestination();

        assertEquals(dataUrl2, dest.toString());
        verifyServer1HealthCount(1);
        verifyServer2HealthCount(1);
    }

    @Test
    public void firstHealthErrorSecondOk() {
        final MultiDestinationProvider mdp = createMultiDestinationProvider();
        server1WhenRequestHealthThenReturn(BAD_REQUEST, null);
        server2WhenRequestHealthThenReturn(OK, getOkResponseString());
        final URI dest = mdp.getDestination();

        assertEquals(dataUrl2, dest.toString());
        verifyServer1HealthCount(1);
        verifyServer2HealthCount(1);
    }

    @Test
    public void firstAndSecondHealthNotOk() {
        final MultiDestinationProvider mdp = createMultiDestinationProvider();
        server1WhenRequestHealthThenReturn(BAD_REQUEST, null);
        server2WhenRequestHealthThenReturn(OK, NOT_OK_RESPONSE_CONTENT);

        try {
            mdp.getDestination();
        } catch (final IllegalStateException e) {
            verifyServer1HealthCount(1);
            verifyServer2HealthCount(1);
            return; // this is wanted
        }
        fail("Should not execute as IllegalStateException should have been thrown");
    }

    @Test
    public void healthRequestOnlyOnceAsTTLNotPassed() {
        final MultiDestinationProvider mdp = createMultiDestinationProvider();
        server1WhenRequestHealthThenReturn(OK, getOkResponseString());
        final URI dest1 = mdp.getDestination();
        assertEquals(dataUrl1, dest1.toString());
        final URI dest2 = mdp.getDestination();

        assertEquals(dataUrl1, dest2.toString());
        verifyServer1HealthCount(1);
        verifyServer2HealthCount(0);
    }

    @Test
    public void healthRequestTwiceAsTTLPassed() {
        final MultiDestinationProvider mdp = createMultiDestinationProvider();
        server1WhenRequestHealthThenReturn(OK, getOkResponseString());

        final URI dest1 = mdp.getDestination();
        assertEquals(dataUrl1, dest1.toString());

        // Wait for TTL to pass
        ThreadUtils.delayMs(1100L);

        final URI dest2 = mdp.getDestination();

        assertEquals(dataUrl1, dest2.toString());
        verifyServer1HealthCount(2);
        verifyServer2HealthCount(0);
    }

    @Test
    public void hostWithoutHealthCheck() {
        final MultiDestinationProvider mdp = createMultiDestinationProviderWithoutHealthCheck();
        final URI dest = mdp.getDestination();
        assertEquals(dataUrl1, dest.toString());
        verifyServer1HealthCount(0);
        verifyServer2HealthCount(0);
    }

    @Test
    public void hostWithoutHealthCheckButUnhealthy() {
        final MultiDestinationProvider mdp = createMultiDestinationProviderWithoutHealthCheck();
        mdp.setHostNotHealthy(mdp.getDestinations().get(0)); // first destination not healthy
        final URI dest = mdp.getDestination();
        assertEquals(dataUrl2, dest.toString());
        verifyServer1HealthCount(0);
        verifyServer2HealthCount(0);
    }

    @Test
    public void hostWithoutUrl() {
        Assertions.assertThrows(fi.livi.digitraffic.tie.service.IllegalArgumentException.class,
            () -> new HostWithHealthCheck(null, null, null, 0, null));
    }

    @Test
    public void hostWithoutDatapath() {
        Assertions.assertThrows(fi.livi.digitraffic.tie.service.IllegalArgumentException.class,
            () -> new HostWithHealthCheck("notnull", null, null, 0, null));
    }

    @Test
    public void hostWithoutHealthOk() {
        Assertions.assertThrows(fi.livi.digitraffic.tie.service.IllegalArgumentException.class,
            () -> new HostWithHealthCheck("notnull", "notnull", "notnull", 0, null));
    }

}
