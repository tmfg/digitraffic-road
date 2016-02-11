package fi.livi.digitraffic.tie.dao;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.LamStation;

@Repository
public interface LamStationRepository extends JpaRepository<LamStation, Long> {
    @EntityGraph("lamstation")
    @Override
    List<LamStation> findAll();
}
