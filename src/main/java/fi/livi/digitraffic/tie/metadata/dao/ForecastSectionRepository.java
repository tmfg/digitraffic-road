package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.ForecastSection;

@Repository
public interface ForecastSectionRepository extends JpaRepository<ForecastSection, Long> {
    @Override
    @Query(value =
            "select fs.natural_id, road_section_number, fs.description, r.natural_id as road_number, srs.natural_id as " +
                    "start_section_number, start_distance, ers.natural_id as end_section_number, fs.end_distance, length\n" +
                    "from forecast_section fs, road r, road_section srs, road_section ers\n" +
                    "where fs.road_id = r.id\n" +
                    "and srs.id = start_road_section_id\n" +
                    "and ers.id = end_road_section_id",
            nativeQuery = true)
    List<ForecastSection> findAll();
}
