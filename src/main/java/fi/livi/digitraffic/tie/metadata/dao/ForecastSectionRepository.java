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
            "select fs.natural_id\n" +
            "     , road_section_number\n" +
            "     , fs.description\n" +
            "     , r.natural_id as road_number\n" +
            "     , srs.natural_id as start_section_number\n" +
            "     , start_distance\n" +
            "     , ers.natural_id as end_section_number\n" +
            "     , fs.end_distance\n" +
            "     , fs.length\n" +
            "from forecast_section fs\n" +
            "inner join road r on r.id = fs.road_id \n" +
            "inner join road_section srs on srs.id = fs.start_road_section_id\n" +
            "inner join road_section ers on ers.id = fs.end_road_section_id\n" +
            "ORDER BY fs.natural_id",
            nativeQuery = true)
    List<ForecastSection> findAll();
}
