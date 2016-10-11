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
    public void equalsIgnoreVersionSucceeds() {
        assertTrue(ForecastSectionNaturalIdHelper.equalsIgnoreVersion(naturalId, "00003_207_666_5"));
        assertFalse(ForecastSectionNaturalIdHelper.equalsIgnoreVersion(naturalId, "00004_207_666_5"));
        assertFalse(ForecastSectionNaturalIdHelper.equalsIgnoreVersion(naturalId, "00003_206_666_5"));
    }
}