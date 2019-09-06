package fi.livi.digitraffic.tie.data.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.data.dto.trafficsigns.TrafficSignHistory;
import fi.livi.digitraffic.tie.data.model.trafficsigns.DeviceData;

@Repository
public interface DeviceDataRepository extends JpaRepository<DeviceData, Long> {
    @Query(value =
        "select id, created_date, device_id, display_value, additional_information, effect_date, cause\n" +
        "from device_data where id in(\n" +
        "select max(id) over (partition by device_id order by effect_date) from device_data\n" +
        ")",
        nativeQuery = true)
    List<DeviceData> findLatestData();

    List<TrafficSignHistory> getDeviceDataByDeviceId(final String deviceId);
}
