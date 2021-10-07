package fi.livi.digitraffic.tie.data.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.dto.v1.tms.TmsRootDataObjectDto;
import fi.livi.digitraffic.tie.helper.AssertHelper;
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

    private long tmsId;

    @BeforeEach
    public void updateData() {
        TestUtils.generateDummyTmsStations(2).forEach(s -> {
            tmsId = s.getRoadStationNaturalId();
            entityManager.persist(s);
        });
        dataStatusService.updateDataUpdated(DataType.getSensorValueUpdatedDataType(RoadStationType.TMS_STATION));
    }

    @Test
    public void findPublishableTmsData() {
        final TmsRootDataObjectDto object = tmsDataService.findPublishableTmsData(false);
        assertNotNull(object);
        assertNotNull(object.dataUpdatedTime);
        AssertHelper.assertCollectionSize(2, object.getTmsStations());
    }

    @Test
    public void findPublishableTmsDataById() {
        final TmsRootDataObjectDto object = tmsDataService.findPublishableTmsData(tmsId);
        assertNotNull(object);
        assertNotNull(object.dataUpdatedTime);
        AssertHelper.assertCollectionSize(1, object.getTmsStations());
    }

    @Test
    public void findPublishableTmsDataByIdNotFound() {
        assertThrows(ObjectNotFoundException.class, () -> {
            tmsDataService.findPublishableTmsData(-1);
        });
    }
}