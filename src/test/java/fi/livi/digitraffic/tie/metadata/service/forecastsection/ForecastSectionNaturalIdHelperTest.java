package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ForecastSectionNaturalIdHelperTest {

    private final String naturalId = "00003_207_010_8";

    @Test
    public void getRoadNumberSucceeds() {
        assertEquals(3, ForecastSectionNaturalIdHelper.getRoadNumber(naturalId));
    }

    @Test
    public void getRoadSectionNumberSucceeds() {
        assertEquals(207, ForecastSectionNaturalIdHelper.getRoadSectionNumber(naturalId));
    }

    @Test
    public void getRoadSectionVersionNumberSucceeds() {
        assertEquals(10, ForecastSectionNaturalIdHelper.getRoadSectionVersionNumber(naturalId));
    }

    @Test
    public void isNaturalIdSucceeds() {
        assertTrue(ForecastSectionNaturalIdHelper.isNaturalId(naturalId));
        assertTrue(ForecastSectionNaturalIdHelper.isNaturalId("03305_067_000_0"));
        assertFalse(ForecastSectionNaturalIdHelper.isNaturalId("03305_067_000_01"));
        assertFalse(ForecastSectionNaturalIdHelper.isNaturalId("03305_067_000"));
        assertFalse(ForecastSectionNaturalIdHelper.isNaturalId("0305_067_000_0"));
        assertFalse(ForecastSectionNaturalIdHelper.isNaturalId("00305_07_0000_0"));
        assertFalse(ForecastSectionNaturalIdHelper.isNaturalId("00305_007_00_00"));
        assertFalse(ForecastSectionNaturalIdHelper.isNaturalId("002305_07_000_0"));
        assertFalse(ForecastSectionNaturalIdHelper.isNaturalId("002305_07_03300"));
        assertFalse(ForecastSectionNaturalIdHelper.isNaturalId("002305507503300"));
        assertFalse(ForecastSectionNaturalIdHelper.isNaturalId("03_05_067_000_0"));
        assertFalse(ForecastSectionNaturalIdHelper.isNaturalId("03005_067_0_0_0"));
    }
}