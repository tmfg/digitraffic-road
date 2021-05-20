package fi.livi.digitraffic.tie.data.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.dto.v1.tms.TmsRootDataObjectDto;
import fi.livi.digitraffic.tie.dto.v1.tms.TmsStationDto;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.service.v1.TmsDataService;

public class TmsDataServiceTest extends AbstractServiceTest {

    @Autowired
    private TmsDataService tmsDataService;

    @Autowired
    private DataStatusService dataStatusService;

    @BeforeEach
    public void updateData() {
        dataStatusService.updateDataUpdated(DataType.getSensorValueUpdatedDataType(RoadStationType.TMS_STATION));
    }

    @Test
    public void findPublishableTmsData() {
        final TmsRootDataObjectDto object = tmsDataService.findPublishableTmsData(false);
        assertNotNull(object);
        assertNotNull(object.dataUpdatedTime);
        assertNotNull(object.getTmsStations());
    }

    @Test
    public void findPublishableTmsDataById() {
        final TmsRootDataObjectDto object = tmsDataService.findPublishableTmsData(23801);
        assertNotNull(object);
        assertNotNull(object.dataUpdatedTime);
        assertNotNull(object.getTmsStations());
        assertThat(object.getTmsStations(), not(emptyCollectionOf(TmsStationDto.class)));
    }

    @Test
    public void findPublishableTmsDataByIdNotFound() {
        assertThrows(ObjectNotFoundException.class, () -> {
            tmsDataService.findPublishableTmsData(-1);
        });
    }
}