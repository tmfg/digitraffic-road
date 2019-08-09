package fi.livi.digitraffic.tie.data.service;

import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.data.dto.tms.TmsRootDataObjectDto;
import fi.livi.digitraffic.tie.data.dto.tms.TmsStationDto;
import fi.livi.digitraffic.tie.metadata.converter.StationSensorConverter;
import fi.livi.digitraffic.tie.metadata.converter.TmsStationMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.metadata.dao.TmsSensorConstantDao;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.DataStatusService;
import fi.livi.digitraffic.tie.metadata.service.RoadDistrictService;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationSensorConstantService;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationService;

@Import({TmsDataService.class, DataStatusService.class, TmsStationService.class, RoadStationSensorService.class,
    TmsStationSensorConstantService.class, RoadStationService.class, RoadDistrictService.class,
    TmsStationMetadata2FeatureConverter.class, CoordinateConverter.class, StationSensorConverter.class,
    TmsSensorConstantDao.class})
public class TmsDataServiceTest extends AbstractServiceTest {

    @Autowired
    private TmsDataService tmsDataService;

    @Autowired
    private DataStatusService dataStatusService;

    @Before
    public void updateData() {
        dataStatusService.updateDataUpdated(DataType.getSensorValueUpdatedDataType(RoadStationType.TMS_STATION));
    }

    @Test
    public void findPublishableTmsData() {
        final TmsRootDataObjectDto object = tmsDataService.findPublishableTmsData(false);
        assertNotNull(object);
        assertNotNull(object.getDataUpdatedTime());
        assertNotNull(object.getTmsStations());
    }

    @Test
    public void findPublishableTmsDataById() {
        final TmsRootDataObjectDto object = tmsDataService.findPublishableTmsData(23801);
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