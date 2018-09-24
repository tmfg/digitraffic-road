package fi.livi.digitraffic.tie.metadata.model;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.util.Assert;

import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._03._12.KameraTyyppi;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2018._03._12.AjoneuvoluokkaTyyppi;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2018._03._12.LaiteTyyppi;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2018._03._12.LamAsemaTyyppi;
import fi.livi.ws.wsdl.lotju.tiesaa._2017._05._02.TiesaaAsemaTyyppi;

@RunWith(JUnit4.class)
public class TypesTest {

    @Test
    public void testAllCameraTypes() {
        for (final KameraTyyppi kt : KameraTyyppi.values()) {
            assertNotNull(CameraType.convertFromKameraTyyppi(kt));
        }
    }

    @Test
    public void testAllCameraStationCollectionStates() {
        for (final fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._03._12.KeruunTILA kt : fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._03._12.KeruunTILA.values()) {
            assertNotNull(CollectionStatus.convertKeruunTila(kt));
        }
    }

    @Test
    public void testAllWeatherStationCollectionStates() {
        for (final fi.livi.ws.wsdl.lotju.tiesaa._2017._05._02.KeruunTILA kt : fi.livi.ws.wsdl.lotju.tiesaa._2017._05._02.KeruunTILA.values()) {
            assertNotNull(CollectionStatus.convertKeruunTila(kt));
        }
    }

    @Test
    public void testAllTmsStationCollectionStates() {
        for (final fi.livi.ws.wsdl.lotju.lammetatiedot._2018._03._12.KeruunTILA kt : fi.livi.ws.wsdl.lotju.lammetatiedot._2018._03._12.KeruunTILA.values()) {
            assertNotNull(CollectionStatus.convertKeruunTila(kt));
        }
    }

    @Test
    public void testAllTmsStationTypes() {
        for (final LamAsemaTyyppi lat : LamAsemaTyyppi.values()) {
            assertNotNull(TmsStationType.convertFromLamasemaTyyppi(lat));
        }
    }

    @Test
    public void testAllCameraRoadStationStates() {
        for (final fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._03._12.TilaTyyppi tt : fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._03._12.TilaTyyppi.values()) {
            assertNotNull(RoadStationState.convertAsemanTila(tt));
        }
    }

    @Test
    public void testAllTmsRoadStationStates() {
        for (final fi.livi.ws.wsdl.lotju.lammetatiedot._2018._03._12.TilaTyyppi tt : fi.livi.ws.wsdl.lotju.lammetatiedot._2018._03._12.TilaTyyppi.values()) {
            assertNotNull(RoadStationState.convertAsemanTila(tt));
        }
    }

    @Test
    public void testAllWeatherRoadStationStates() {
        for (final fi.livi.ws.wsdl.lotju.tiesaa._2017._05._02.TilaTyyppi tt : fi.livi.ws.wsdl.lotju.tiesaa._2017._05._02.TilaTyyppi.values()) {
            assertNotNull(RoadStationState.convertAsemanTila(tt));
        }
    }

    @Test
    public void testAllWeatherStationTypes() {
        for (final TiesaaAsemaTyyppi tsat : TiesaaAsemaTyyppi.values()) {
            assertNotNull(WeatherStationType.fromTiesaaAsemaTyyppi(tsat));
        }
    }

    @Test
    public void testAllTmsCalculatorDeviceTypes() {
        for (final LaiteTyyppi lt : LaiteTyyppi.values()) {
            assertNotNull(CalculatorDeviceType.convertFromLaiteTyyppi(lt));
        }
    }

    @Test
    public void testAllVehiVehicleClasscleClassTypes() {
        for (final AjoneuvoluokkaTyyppi at : AjoneuvoluokkaTyyppi.values()) {
            assertNotNull(VehicleClass.convertAjoneuvoluokka(at));
        }
    }


}
