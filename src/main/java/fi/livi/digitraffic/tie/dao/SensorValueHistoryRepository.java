package fi.livi.digitraffic.tie.dao;

import java.time.ZonedDateTime;
import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.v1.SensorValueHistoryDto;
import fi.livi.digitraffic.tie.model.SensorValueHistory;

@Repository
public interface SensorValueHistoryRepository extends JpaRepository<SensorValueHistory, Long> {

    // TODO! Try to find proper fetch size
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<SensorValueHistoryDto> findByRoadStationIdAndMeasuredTimeIsGreaterThanOrderByMeasuredTimeAsc(final long id, final ZonedDateTime since);

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<SensorValueHistoryDto> findByRoadStationIdAndMeasuredTimeBetweenOrderByMeasuredTimeAsc(final long id, final ZonedDateTime from, final ZonedDateTime to);

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<SensorValueHistoryDto> findByRoadStationIdAndAndSensorIdAndMeasuredTimeIsGreaterThanOrderByMeasuredTimeAsc(final long station, final long sensor, final ZonedDateTime since);
}
