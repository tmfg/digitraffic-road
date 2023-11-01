package fi.livi.digitraffic.tie.dao.v1.tms;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractJpaTest;
import fi.livi.digitraffic.tie.dao.tms.TmsStationDatex2Repository;
import fi.livi.digitraffic.tie.model.roadstation.CollectionStatus;

public class TmsStationDatex2RepositoryTest extends AbstractJpaTest {

    @Autowired
    private TmsStationDatex2Repository tmsStationDatex2Repository;

    @Test
    public void findDistinctByRoadStationPublishableIsTrueOrderByRoadStation_NaturalId() {
        tmsStationDatex2Repository.findDistinctByRoadStationPublishableIsTrueOrderByNaturalId();
    }

    @Test
    public void findDistinctByRoadStationIsPublicIsTrueAndRoadStationCollectionStatusIsOrderByRoadStation_NaturalId() {
        tmsStationDatex2Repository.findDistinctByRoadStationIsPublicIsTrueAndRoadStationCollectionStatusIsOrderByNaturalId(CollectionStatus.GATHERING);
    }

    @Test
    public void findDistinctByRoadStationIsPublicIsTrueOrderByRoadStation_NaturalId() {
        tmsStationDatex2Repository.findDistinctByRoadStationIsPublicIsTrueOrderByNaturalId();
    }
}
