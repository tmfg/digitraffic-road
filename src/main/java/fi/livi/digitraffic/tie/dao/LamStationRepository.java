package fi.livi.digitraffic.tie.dao;

import java.util.List;

import fi.livi.digitraffic.tie.model.LamStation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LamStationRepository extends JpaRepository<LamStation, Long> {
    @EntityGraph("lamStation")
    @Override
    List<LamStation> findAll();

    List<LamStation> findByRoadStationObsoleteFalse();
}
