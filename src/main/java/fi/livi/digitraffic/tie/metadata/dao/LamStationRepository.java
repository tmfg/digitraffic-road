package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.LamStation;

@Repository
public interface LamStationRepository extends JpaRepository<LamStation, Long> {

    List<LamStation> findByRoadStationObsoleteFalseAndRoadStationIsPublicTrueAndLotjuIdIsNotNullOrderByRoadStation_NaturalId();

    LamStation findByLotjuId(long lamStationLotjuId);

    LamStation findByRoadStation_NaturalId(long roadStationNaturalId);

    List<LamStation> findByLotjuIdIn(List<Long> lamLotjuIds);

    @Query("SELECT CASE WHEN COUNT(lam) > 0 THEN TRUE ELSE FALSE END\n" +
           "FROM LamStation lam\n" +
           "WHERE lam.naturalId = ?1\n" +
           "  AND lam.roadStation.obsolete = false\n" +
           "  AND lam.roadStation.isPublic = true")
    boolean lamExistsWithLamNaturalId(long lamNaturalId);

    @Query("SELECT CASE WHEN COUNT(lam) > 0 THEN TRUE ELSE FALSE END\n" +
           "FROM LamStation lam\n" +
           "WHERE lam.roadStation.naturalId = ?1\n" +
           "  AND lam.roadStation.obsolete = false\n" +
           "  AND lam.roadStation.isPublic = true")
    boolean lamExistsWithRoadStationNaturalId(long roadStationNaturalId);
}
