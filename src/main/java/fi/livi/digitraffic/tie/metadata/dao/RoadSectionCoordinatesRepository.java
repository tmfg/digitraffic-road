package fi.livi.digitraffic.tie.metadata.dao;

import fi.livi.digitraffic.tie.metadata.model.RoadSectionCoordinates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoadSectionCoordinatesRepository extends JpaRepository<RoadSectionCoordinates, Long> {

    @Query(value = "SELECT count(*) FROM ROAD_SECTION_COORDINATES", nativeQuery = true)
    Long getRoadSectionCoordinatesCount();
}
