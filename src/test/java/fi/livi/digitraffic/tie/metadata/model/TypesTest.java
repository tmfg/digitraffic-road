package fi.livi.digitraffic.tie.metadata.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.util.Assert;

import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.KameraTyyppi;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.TilaTyyppi;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29.LamAsemaTyyppi;
import fi.livi.ws.wsdl.lotju.tiesaa._2015._09._29.TiesaaAsemaTyyppi;

@RunWith(JUnit4.class)
public class TypesTest {

    @Test
    public void testAllCameraTypes() {
        for (final KameraTyyppi kt : KameraTyyppi.values()) {
            Assert.notNull(CameraType.convertFromKameraTyyppi(kt));
        }
    }

    @Test
    public void testAllLamStationTypes() {
        for (final LamAsemaTyyppi lat : LamAsemaTyyppi.values()) {
            Assert.notNull(LamStationType.convertFromLamasemaTyyppi(lat));
        }
    }

    @Test
    public void testAllRoadStationStates() {
        for (final TilaTyyppi tt : TilaTyyppi.values()) {
            Assert.notNull(RoadStationState.convertAsemanTila(tt));
        }
    }

    @Test
    public void testAllWeatherStationTypes() {
        for (final TiesaaAsemaTyyppi tsat : TiesaaAsemaTyyppi.values()) {
            Assert.notNull(WeatherStationType.fromTiesaaAsemaTyyppi(tsat));
        }
    }

}
