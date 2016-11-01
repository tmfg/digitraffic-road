package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.TmsStation;

@Repository
public interface TmsStationRepository extends JpaRepository<TmsStation, Long> {

    List<TmsStation> findByRoadStationObsoleteFalseAndRoadStationIsPublicTrueAndLotjuIdIsNotNullOrderByRoadStation_NaturalId();

    TmsStation findByLotjuId(long tmsStationLotjuId);

    TmsStation findByRoadStation_NaturalId(long roadStationNaturalId);

    List<TmsStation> findByLotjuIdIn(List<Long> tmsLotjuIds);

    @Query("SELECT CASE WHEN COUNT(tms) > 0 THEN TRUE ELSE FALSE END\n" +
           "FROM TmsStation tms\n" +
           "WHERE tms.naturalId = ?1\n" +
           "  AND tms.roadStation.obsolete = false\n" +
           "  AND tms.roadStation.isPublic = true")
    boolean tmsExistsWithLamNaturalId(long tmsNaturalId);

    @Query("SELECT CASE WHEN COUNT(tms) > 0 THEN TRUE ELSE FALSE END\n" +
           "FROM TmsStation tms\n" +
           "WHERE tms.roadStation.naturalId = ?1\n" +
           "  AND tms.roadStation.obsolete = false\n" +
           "  AND tms.roadStation.isPublic = true")
    boolean tmsExistsWithRoadStationNaturalId(long roadStationNaturalId);
}
