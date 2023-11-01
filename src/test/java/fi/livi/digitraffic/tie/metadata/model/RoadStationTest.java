package fi.livi.digitraffic.tie.metadata.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.ThreadUtils;
import fi.livi.digitraffic.tie.model.roadstation.RoadStation;

public class RoadStationTest extends AbstractTest {

    /* Publicity With time */

    @Test
    public void isAllowedToSetPublicityTimeForCameraStation() {
        final RoadStation rs = RoadStation.createCameraStation();

        assertDoesNotThrow(() -> updatePublicityWithTime(rs));
    }

    @Test
    public void isNotAllowedToSetPublicityTimeForWeatherStation() {
        final RoadStation rs = RoadStation.createWeatherStation();

        assertThrows(IllegalStateException.class, () -> updatePublicityWithTime(rs));
    }

    @Test
    public void isNotAllowedToSetPublicityTimeForTmsStation() {
        final RoadStation rs = RoadStation.createWeatherStation();

        assertThrows(IllegalStateException.class, () -> updatePublicityWithTime(rs));
    }

    /* Publicity without time */

    @Test
    public void isAllowedToSetPublicityForCameraStation() {
        final RoadStation rs = RoadStation.createCameraStation();

        assertDoesNotThrow(() -> updatePublicityWithoutTime(rs));
    }

    @Test
    public void isAllowedToSetPublicityForWeatherStation() {
        final RoadStation rs = RoadStation.createWeatherStation();

        assertDoesNotThrow(() -> updatePublicityWithoutTime(rs));
    }

    @Test
    public void isAllowedToSetPublicityForTmsStation() {
        final RoadStation rs = RoadStation.createWeatherStation();

        assertDoesNotThrow(() -> updatePublicityWithoutTime(rs));
    }

    @Test
    public void initialState() {
        final RoadStation rs = RoadStation.createCameraStation();
        assertFalse(rs.isPublicPrevious());
        assertFalse(rs.isPublicNow());
    }

    @Test
    public void publicityUpdateNow() {
        final RoadStation rs = RoadStation.createCameraStation();

        rs.updatePublicity(true, getNow());
        assertFalse(rs.isPublicPrevious());
        assertTrue(rs.isPublicNow());

        rs.updatePublicity(false, getNow());
        assertTrue(rs.isPublicPrevious());
        assertFalse(rs.isPublicNow());
    }

    @Test
    public void publicityUpdateInPast() {
        final RoadStation rs = RoadStation.createCameraStation();
        rs.updatePublicity(true, getNow().minusHours(1));
        assertFalse(rs.isPublicPrevious());
        assertTrue(rs.isPublicNow());
    }

    @Test
    public void publicityUpdateInFuture() throws InterruptedException {
        final RoadStation rs = RoadStation.createCameraStation();
        // public at the beginning
        rs.updatePublicity(true);

        final ZonedDateTime secretInFuture = getNow().plusSeconds(1);
        rs.updatePublicity(false, secretInFuture);
        assertTrue(rs.isPublicPrevious());
        assertTrue(rs.isPublicNow());
        assertFalse(rs.internalIsPublic());

        // Wait for secretFrom time to pass -> RoadStation changes to not public
        while ( ZonedDateTime.now().isBefore(secretInFuture) ) {
            ThreadUtils.delayMs(100);
        }
        assertFalse(rs.isPublicNow());
    }

    @Test
    public void multiplePublicityUpdatesInFuture() throws InterruptedException {
        final RoadStation rs = RoadStation.createCameraStation();
        // public at the beginning
        rs.updatePublicity(true);

        final ZonedDateTime secretInFuture1 = getNow().plusSeconds(1);
        rs.updatePublicity(false, secretInFuture1);
        assertTrue(rs.isPublicPrevious());
        assertTrue(rs.isPublicNow());
        assertFalse(rs.internalIsPublic());

        // Previous value should not be updated if start time is in future
        final ZonedDateTime secretInFuture2 = getNow().plusSeconds(2);
        rs.updatePublicity(false, secretInFuture2);
        assertTrue(rs.isPublicPrevious());
        assertTrue(rs.isPublicNow());
        assertFalse(rs.internalIsPublic());

        // Wait for secretInFuture1 time to pass -> RoadStation should not change to secret
        // as second update moved change to secretInFuture2
        while ( ZonedDateTime.now().isBefore(secretInFuture1) ) {
            ThreadUtils.delayMs(100);
        }
        assertTrue(rs.isPublicNow());

        // Wait for secretInFuture2 time to pass -> RoadStation changes to not public
        while ( ZonedDateTime.now().isBefore(secretInFuture2) ) {
            ThreadUtils.delayMs(100);
        }
        assertFalse(rs.isPublicNow());
    }

    @Test
    public void multiplePublicityUpdatesInFutureAndBackToPublic() throws InterruptedException {
        final RoadStation rs = RoadStation.createCameraStation();
        // public at the beginning
        rs.updatePublicity(true);

        final ZonedDateTime secretInFuture1 = getNow().plusSeconds(1);
        rs.updatePublicity(false, secretInFuture1);
        final ZonedDateTime secretInFuture2 = getNow().plusSeconds(2);
        rs.updatePublicity(false, secretInFuture2);
        assertTrue(rs.isPublicPrevious());
        assertTrue(rs.isPublicNow());
        assertFalse(rs.internalIsPublic());

        // Future public, previous public as secretInFuture not passed
        final ZonedDateTime publicInFuture = getNow().plusSeconds(3);
        rs.updatePublicity(true, publicInFuture);
        assertTrue(rs.isPublicPrevious());
        assertTrue(rs.isPublicNow());
        assertTrue(rs.internalIsPublic());

        // Wait for secretInFuture1 time to pass -> RoadStation should not change to secret
        while ( ZonedDateTime.now().isBefore(secretInFuture1) ) {
            ThreadUtils.delayMs(100);
        }
        assertTrue(rs.isPublicNow());

        // Wait for secretInFuture2 time to pass -> RoadStation changes to not public
        while ( ZonedDateTime.now().isBefore(secretInFuture2) ) {
            ThreadUtils.delayMs(100);
        }
        assertTrue(rs.isPublicNow());

        // Wait for publicInFuture time to pass -> RoadStation stays public
        while ( ZonedDateTime.now().isBefore(publicInFuture) ) {
            ThreadUtils.delayMs(100);
        }
        assertTrue(rs.isPublicNow());
    }

    private void updatePublicityWithTime(final RoadStation rs) {
        rs.updatePublicity(true, ZonedDateTime.now());
    }

    private void updatePublicityWithoutTime(final RoadStation rs) {
        rs.updatePublicity(true);
    }

    private ZonedDateTime getNow() {
        return DateHelper.toZonedDateTimeAtUtc(Instant.now().truncatedTo(ChronoUnit.SECONDS));
    }
}
