package fi.livi.digitraffic.tie.dao.roadstation;

import java.time.ZonedDateTime;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.roadstation.SensorValueHistory;
import jakarta.persistence.QueryHint;

@Repository
public interface SensorValueHistoryRepository extends JpaRepository<SensorValueHistory, Long> {
    // TODO! Try to find proper fetch size
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    Stream<SensorValueHistory> streamAllByRoadStationIdAndMeasuredTimeIsGreaterThanOrderByMeasuredTimeAsc(final long id, final ZonedDateTime since);

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    Stream<SensorValueHistory> streamAllByRoadStationIdAndMeasuredTimeBetweenOrderByMeasuredTimeAsc(final long id, final ZonedDateTime from, final ZonedDateTime to);

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    Stream<SensorValueHistory> streamAllByRoadStationIdAndAndSensorIdAndMeasuredTimeIsGreaterThanOrderByMeasuredTimeAsc(final long station, final long sensor, final ZonedDateTime since);

    Stream<SensorValueHistory> streamAllByMeasuredTimeGreaterThanEqualAndMeasuredTimeLessThanOrderByMeasuredTimeAsc(final ZonedDateTime from, final ZonedDateTime to);
}
