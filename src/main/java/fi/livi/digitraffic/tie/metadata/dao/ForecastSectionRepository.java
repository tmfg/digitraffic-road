package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSection;

@Repository
public interface ForecastSectionRepository extends JpaRepository<ForecastSection, Long> {

    List<ForecastSection> findAllByOrderByNaturalIdAsc();
}
