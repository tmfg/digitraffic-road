package fi.livi.digitraffic.tie.dao.weather;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.roadstation.CollectionStatus;
import fi.livi.digitraffic.tie.model.weather.WeatherStation;
import jakarta.persistence.QueryHint;

@Repository
public interface WeatherStationRepository extends JpaRepository<WeatherStation, Long> {

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = { "roadStation", "roadStation.roadAddress"})
    List<WeatherStation> findAll();

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = {"roadStation", "roadStation.roadAddress"})
    List<WeatherStation> findByRoadStationPublishableIsTrueOrderByRoadStation_NaturalId();

    @EntityGraph(attributePaths = {"roadStation", "roadStation.roadAddress"})
    WeatherStation findByLotjuId(Long lotjuId);

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = {"roadStation", "roadStation.roadAddress"})
    WeatherStation findByRoadStationIsPublicIsTrueAndRoadStation_NaturalId(final Long naturalId);

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = {"roadStation", "roadStation.roadAddress"})
    List<WeatherStation> findByRoadStationIsPublicIsTrueAndRoadStationCollectionStatusIsOrderByRoadStation_NaturalId(final CollectionStatus removedPermanently);

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = {"roadStation", "roadStation.roadAddress"})
    List<WeatherStation> findByRoadStationIsPublicIsTrueOrderByRoadStation_NaturalId();

    WeatherStation findByRoadStation_NaturalIdAndRoadStationPublishableIsTrue(final long roadStationNaturalId);

    @Query(value = """
            select max(src.modified) as modified
            from (select max(ws.modified) as modified
                  from weather_station ws
                  union
                  select max(rs.modified) as modified
                  from road_station rs
                  where rs.type = 'WEATHER_STATION'
                  union
                  select max(rss.modified) as modified
                  from road_station_sensors rss
                  where exists(select null from road_station rs
                               where rs.id = rss.road_station_id
                                 and rs.type = 'WEATHER_STATION')
            ) src""", nativeQuery = true)
    Instant getLastUpdated();
}
