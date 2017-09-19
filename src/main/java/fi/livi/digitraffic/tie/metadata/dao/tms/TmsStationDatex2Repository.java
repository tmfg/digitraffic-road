package fi.livi.digitraffic.tie.metadata.dao.tms;

import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;

import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.model.TmsStation;

public interface TmsStationDatex2Repository extends JpaRepository<TmsStation, Long> {

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = { "roadStation", "roadDistrict", "roadStation.roadStationSensors"})
    List<TmsStation> findDistinctByRoadStationPublishableIsTrueOrderByRoadStation_NaturalId();

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = {"roadStation", "roadDistrict", "roadStation.roadStationSensors"})
    List<TmsStation> findDistinctByRoadStationIsPublicIsTrueAndRoadStationCollectionStatusIsOrderByRoadStation_NaturalId(final CollectionStatus collectionStatus);

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = {"roadStation", "roadDistrict", "roadStation.roadStationSensors"})
    List<TmsStation> findDistinctByRoadStationIsPublicIsTrueOrderByRoadStation_NaturalId();
}
