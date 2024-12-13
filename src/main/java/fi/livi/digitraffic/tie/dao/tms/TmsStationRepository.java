package fi.livi.digitraffic.tie.dao.tms;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.roadstation.CollectionStatus;
import fi.livi.digitraffic.tie.model.tms.TmsStation;
import jakarta.persistence.QueryHint;

@Repository
public interface TmsStationRepository extends JpaRepository<TmsStation, Long> {

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    @EntityGraph(attributePaths = {"roadStation", "roadStation.roadAddress"})
    List<TmsStation> findAll();

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = {"roadStation", "roadStation.roadAddress"})
    List<TmsStation> findByRoadStationIsPublicIsTrueAndRoadStationCollectionStatusIsOrderByRoadStation_NaturalId(final CollectionStatus collectionStatus);

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = {"roadStation", "roadStation.roadAddress"})
    List<TmsStation> findByRoadStationPublishableIsTrueOrderByRoadStation_NaturalId();

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = {"roadStation", "roadStation.roadAddress"})
    List<TmsStation> findByRoadStationIsPublicIsTrueOrderByRoadStation_NaturalId();

    @EntityGraph(attributePaths = {"roadStation", "roadStation.roadAddress"})
    TmsStation findByRoadStationPublishableIsTrueAndRoadStation_NaturalId(final long roadStationNaturalId);

    @EntityGraph(attributePaths = {"roadStation", "roadStation.roadAddress"})
    TmsStation findByLotjuId(Long lotjuId);

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @Query("""
            select p.lotjuId
            from #{#entityName} p""")
    List<Long> findAllTmsStationsLotjuIds();

    @Query(value = """
            select max(src.modified) as modified
            from (select max(modified) as modified
                  from tms_station
                  union
                  select max(modified) as modified
                  from road_station
                  where road_station_type = 'TMS_STATION'
                  union
                  select max(rss.modified) as modified
                  from road_station_sensors rss
                  where exists(select null from road_station rs
                               where rs.id = rss.road_station_id
                                 and rs.road_station_type = 'TMS_STATION')
            ) src""", nativeQuery = true)
    Instant getLastUpdated();

}
