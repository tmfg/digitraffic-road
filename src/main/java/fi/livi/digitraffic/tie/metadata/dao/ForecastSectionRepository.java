package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSection;

@Repository
public interface ForecastSectionRepository extends JpaRepository<ForecastSection, Long> {
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    @EntityGraph(attributePaths = { "road", "startRoadSection", "endRoadSection", "startRoadSection.roadDistrict", "endRoadSection.roadDistrict",
                                    "forecastSectionCoordinateLists" }, type = EntityGraph.EntityGraphType.LOAD)
    List<ForecastSection> findDistinctBy(final Sort sort);
}
