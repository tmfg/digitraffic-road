package fi.livi.digitraffic.tie.dao.roadstation;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

import org.hibernate.jpa.AvailableHints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.roadstation.SensorValueHistory;
import jakarta.persistence.QueryHint;

@Repository
public interface SensorValueHistoryRepository extends JpaRepository<SensorValueHistory, Long> {
    @QueryHints(@QueryHint(name=AvailableHints.HINT_FETCH_SIZE, value="1000"))
    List<SensorValueHistory> getAllByRoadStationIdAndMeasuredTimeBetweenOrderByMeasuredTimeAsc(final long id, final Instant from, final Instant to);

    @QueryHints(@QueryHint(name=AvailableHints.HINT_FETCH_SIZE, value="1000"))
    List<SensorValueHistory> getAllByRoadStationIdAndAndSensorIdAndMeasuredTimeBetweenOrderByMeasuredTimeAsc(final long station, final long sensor, final Instant from, final Instant to);

    @QueryHints(@QueryHint(name=AvailableHints.HINT_FETCH_SIZE, value="1000"))
    List<SensorValueHistory> getAllByMeasuredTimeGreaterThanEqualAndMeasuredTimeLessThanOrderByMeasuredTimeAsc(final Instant from, final Instant to);
}
