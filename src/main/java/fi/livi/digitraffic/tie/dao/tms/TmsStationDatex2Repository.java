package fi.livi.digitraffic.tie.dao.tms;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;

import fi.livi.digitraffic.tie.model.roadstation.CollectionStatus;
import fi.livi.digitraffic.tie.model.tms.TmsStation;
import jakarta.persistence.QueryHint;

public interface TmsStationDatex2Repository extends JpaRepository<TmsStation, Long> {
    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = {"roadStation", "roadStation.roadStationSensors"})
    List<TmsStation> findDistinctByRoadStationPublishableIsTrueOrderByNaturalId();

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = {"roadStation", "roadStation.roadStationSensors"})
    List<TmsStation> findDistinctByRoadStationIsPublicIsTrueAndRoadStationCollectionStatusIsOrderByNaturalId(final CollectionStatus collectionStatus);

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = {"roadStation", "roadStation.roadStationSensors"})
    List<TmsStation> findDistinctByRoadStationIsPublicIsTrueOrderByNaturalId();
}
