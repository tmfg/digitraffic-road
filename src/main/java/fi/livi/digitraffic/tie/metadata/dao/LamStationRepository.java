package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.LamStation;

@Repository
public interface LamStationRepository extends JpaRepository<LamStation, Long> {

    List<LamStation> findByRoadStationObsoleteFalseAndRoadStationIsPublicTrue();

    LamStation findByLotjuId(long lamStationLotjuId);

    LamStation findByRoadStation_NaturalId(long roadStationNaturalId);
}
