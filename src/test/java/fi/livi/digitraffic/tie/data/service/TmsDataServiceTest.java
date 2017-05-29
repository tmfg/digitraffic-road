package fi.livi.digitraffic.tie.data.service;

import static fi.livi.digitraffic.tie.metadata.model.RoadStationType.TMS_STATION;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.data.dto.tms.TmsRootDataObjectDto;
import fi.livi.digitraffic.tie.data.dto.tms.TmsStationDto;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
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
        final Map<Long, TmsStation> stations = tmsStationService.findAllPublishableTmsStationsMappedByLotjuId();
        final List<RoadStationSensor> availableSensors = roadStationSensorService.findAllNonObsoleteRoadStationSensors(TMS_STATION);

        stations.values().forEach(station -> {
            final RoadStation rs = station.getRoadStation();
            availableSensors.forEach(s -> {
                if (!rs.getRoadStationSensors().contains(s)) {
                    rs.getRoadStationSensors().add(s);
                }
            });
        });
    }

    @Test
    public void findPublishableTmsData() {
        final TmsRootDataObjectDto object = tmsDataService.findPublishableTmsData(false);
        assertNotNull(object);
        assertNotNull(object.getDataUpdatedTime());
        assertNotNull(object.getTmsStations());
        assertNotNull(object.getTmsStations());
    }

    @Test
    public void findPublishableTmsDataById() {
        final TmsRootDataObjectDto object = tmsDataService.findPublishableTmsData(-23001);
        assertNotNull(object);
        assertNotNull(object.getDataUpdatedTime());
        assertNotNull(object.getTmsStations());
        Assert.assertThat(object.getTmsStations(), not(emptyCollectionOf(TmsStationDto.class)));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void findPublishableTmsDataByIdNotFound() {
        tmsDataService.findPublishableTmsData(-1);
    }
}