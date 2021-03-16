package fi.livi.digitraffic.tie.dao.v2;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.v1.trafficsigns.TrafficSignHistory;
import fi.livi.digitraffic.tie.model.v2.trafficsigns.DeviceData;

import javax.persistence.QueryHint;

@Repository
public interface V2DeviceDataRepository extends JpaRepository<DeviceData, Long> {
    @Query(value =
        "select distinct first_value(id) over (partition by device_id order by effect_date desc) from device_data",
        nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Long> findLatestData();

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    @EntityGraph(attributePaths = "rows")
    List<DeviceData> findDistinctByIdIn(final List<Long> id);

    @Query(value =
        "select distinct first_value(id) over (order by effect_date desc) from device_data where device_id = :deviceId",
        nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Long> findLatestData(final String deviceId);

    List<TrafficSignHistory> getDeviceDataByDeviceIdOrderByEffectDateDesc(final String deviceId);
}
