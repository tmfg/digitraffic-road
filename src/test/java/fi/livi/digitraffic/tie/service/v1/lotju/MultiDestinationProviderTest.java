package fi.livi.digitraffic.tie.service.v1.lotju;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

public class MultiDestinationProviderTest extends AbstractMultiDestinationProviderTest {

    @Test
    public void firstHealthOk() {
        final MultiDestinationProvider mdp = createMultiDestinationProvider();
        server1WhenRequestHealthThenReturn(OK, OK_CONTENT);
        final URI dest = mdp.getDestination();

        assertEquals(dataUrl1, dest.toString());
        verifyServer1HealthCount(1);
        verifyServer2HealthCount(0);
    }

    @Test
    public void firstHealthNotOkSecondOk() {
        final MultiDestinationProvider mdp = createMultiDestinationProvider();
        server1WhenRequestHealthThenReturn(OK, NOT_OK_CONTENT);
        server2WhenRequestHealthThenReturn(OK, OK_CONTENT);
        final URI dest = mdp.getDestination();

        assertEquals(dataUrl2, dest.toString());
        verifyServer1HealthCount(1);
        verifyServer2HealthCount(1);
    }

    @Test
    public void firstHealthErrorSecondOk() {
        final MultiDestinationProvider mdp = createMultiDestinationProvider();
        server1WhenRequestHealthThenReturn(BAD_REQUEST, null);
        server2WhenRequestHealthThenReturn(OK, OK_CONTENT);
        final URI dest = mdp.getDestination();

        assertEquals(dataUrl2, dest.toString());
        verifyServer1HealthCount(1);
        verifyServer2HealthCount(1);
    }

    @Test(expected = IllegalStateException.class)
    public void firstAndSecondHealthNotOk() {
        final MultiDestinationProvider mdp = createMultiDestinationProvider();
        server1WhenRequestHealthThenReturn(BAD_REQUEST, null);
        server2WhenRequestHealthThenReturn(OK, NOT_OK_CONTENT);
        try {
            mdp.getDestination();
        } catch (final IllegalStateException e) {
            verifyServer1HealthCount(1);
            verifyServer2HealthCount(1);
            throw e;
        }
        Assert.fail("Should not execute as IllegalStateException should have been thrown");
    }

    @Test
    public void healthRequestOnlyOnceAsTTLNotPassed() {
        final MultiDestinationProvider mdp = createMultiDestinationProvider();
        server1WhenRequestHealthThenReturn(OK, OK_CONTENT);
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
        server1WhenRequestHealthThenReturn(OK, OK_CONTENT);

        final URI dest1 = mdp.getDestination();
        assertEquals(dataUrl1, dest1.toString());
        // Wait for TTL to pass
        try {
            Thread.sleep(1100L);
        } catch (InterruptedException e) {
            // empty
        }
        final URI dest2 = mdp.getDestination();

        assertEquals(dataUrl1, dest2.toString());
        verifyServer1HealthCount(2);
        verifyServer2HealthCount(0);
    }
}
