package fi.livi.digitraffic.tie.metadata.dao;

import fi.livi.digitraffic.tie.metadata.model.ForecastSectionCoordinates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoadSectionCoordinatesRepository extends JpaRepository<ForecastSectionCoordinates, Long> {

}
