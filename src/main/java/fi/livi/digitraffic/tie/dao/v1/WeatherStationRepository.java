package fi.livi.digitraffic.tie.dao.v1;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.QueryHint;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.CollectionStatus;
import fi.livi.digitraffic.tie.model.v1.WeatherStation;

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

    @Query(value =
       "select max(src.modified) as modified\n" +
       "from (select max(modified) as modified\n" +
       "      from weather_station\n" +
       "      union\n" +
       "      select max(modified) as modified\n" +
       "      from road_station\n" +
       "      where road_station_type = 'WEATHER_STATION'\n" +
       "      union\n" +
       "      select max(rss.modified) as modified\n" +
       "      from road_station_sensors rss\n" +
       "      where exists(select null from road_station rs\n" +
       "                   where rs.id = rss.road_station_id\n" +
       "                     and rs.road_station_type = 'WEATHER_STATION')\n" +
       ") src", nativeQuery = true)
    Instant getLastUpdated();
}
