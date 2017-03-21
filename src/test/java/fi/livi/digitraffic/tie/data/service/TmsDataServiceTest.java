package fi.livi.digitraffic.tie.data.service;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.data.dto.tms.TmsRootDataObjectDto;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.TmsStation;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationService;

public class TmsDataServiceTest extends AbstractTest {

    @Autowired
    private TmsDataService tmsDataService;

    @Autowired
    private TmsStationService tmsStationService;

    @Autowired
    private RoadStationSensorService roadStationSensorService;

    @Before
    public void initData() {
        Map<Long, TmsStation> stations =
                tmsStationService.findAllPublishableTmsStationsMappedByLotjuId();
        List<RoadStationSensor> availableSensors =
                roadStationSensorService.findAllNonObsoleteRoadStationSensors(RoadStationType.TMS_STATION);
        stations.values().forEach(station -> {
            RoadStation rs = station.getRoadStation();
            availableSensors.forEach(s -> {
                if (!rs.getRoadStationSensors().contains(s)) {
                    rs.getRoadStationSensors().add(s);
                }
            });
        });
    }

    @Test
    public void testFindPublicTmsData()  {
        final TmsRootDataObjectDto object = tmsDataService.findPublishableTmsData(false);
        Assert.notNull(object);
        Assert.notNull(object.getDataUpdatedTime());
        Assert.notNull(object.getTmsStations());
        Assert.notEmpty(object.getTmsStations());
    }

    @Test
    public void testFindPublicTmsDataById()  {
        final TmsRootDataObjectDto object = tmsDataService.findPublishableTmsData(23001);
        Assert.notNull(object);
        Assert.notNull(object.getDataUpdatedTime());
        Assert.notNull(object.getTmsStations());
        Assert.notEmpty(object.getTmsStations());
    }
}
