package fi.livi.digitraffic.tie.dao.roadstation;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.roadstation.RoadStation;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import jakarta.persistence.QueryHint;

@Repository
public interface RoadStationRepository extends JpaRepository<RoadStation, Long>{

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    @EntityGraph(attributePaths = { "roadAddress" }, type = EntityGraph.EntityGraphType.LOAD)
    List<RoadStation> findByType(RoadStationType type);

    @Query("""
            SELECT CASE WHEN count(rs) > 0 THEN TRUE ELSE FALSE END
            FROM RoadStation rs
            WHERE rs.publishable = true
              AND rs.type = :roadStationType
              AND rs.naturalId = :roadStationNaturalId""")
    boolean isPublishableRoadStation(@Param("roadStationNaturalId")
                                     final long roadStationNaturalId,
                                     @Param("roadStationType")
                                     final RoadStationType roadStationType);

    RoadStation findByTypeAndNaturalId(final RoadStationType type, final Long naturalId);

    RoadStation findByTypeAndLotjuId(final RoadStationType tmsStation, final Long id);

    @Query("""
            SELECT rs.id
            FROM RoadStation rs
            WHERE rs.naturalId = :naturalId""")
    Optional<Long> findByRoadStationId(@Param("naturalId") final long naturalId);

    default void checkIsPublishableRoadStation(final long roadStationNaturalId, final RoadStationType type) {
        if ( !isPublishableRoadStation(roadStationNaturalId, type) ) {
            throw new ObjectNotFoundException(type.name(), roadStationNaturalId);
        }
    }
    default void checkIsPublishableTmsRoadStation(final long roadStationNaturalId) {
        checkIsPublishableRoadStation(roadStationNaturalId, RoadStationType.TMS_STATION);
    }
}
