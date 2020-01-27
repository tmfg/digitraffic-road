package fi.livi.digitraffic.tie.dao;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.v1.SensorValueHistoryDto;
import fi.livi.digitraffic.tie.model.SensorValueHistory;

@Repository
public interface SensorValueHistoryRepository extends JpaRepository<SensorValueHistory, Long> {

    List<SensorValueHistoryDto> streamByRoadStationIdAndMeasuredTimeIsGreaterThanOrderByMeasuredTimeAsc(final long id, final ZonedDateTime since);

    List<SensorValueHistoryDto> streamByRoadStationIdAndAndSensorIdAndMeasuredTimeIsGreaterThanOrderByMeasuredTimeAsc(final long station, final long sensor, final ZonedDateTime since);
}
