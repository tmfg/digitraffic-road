package fi.livi.digitraffic.tie.dao.v2;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.v1.trafficsigns.TrafficSignHistory;
import fi.livi.digitraffic.tie.model.v2.variablesign.DeviceData;

import javax.persistence.QueryHint;

@Repository
public interface V2DeviceDataRepository extends JpaRepository<DeviceData, Long> {
    @Query(value =
        "select distinct on (device_id) id from device_data " +
        "where effect_date > now() - interval '7 days' " +
        "order by device_id, effect_date desc",
        nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="10000"))
    List<Long> findLatestData();

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="10000"))
    @EntityGraph(attributePaths = "rows")
    List<DeviceData> findDistinctByIdIn(final List<Long> id);

    @Query(value =
        "select id from device_data where device_id = :deviceId order by effect_date desc limit 1",
        nativeQuery = true)
    List<Long> findLatestData(final String deviceId);

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="10000"))
    @EntityGraph(attributePaths = "rows")
    List<TrafficSignHistory> getDeviceDataByDeviceIdOrderByEffectDateDesc(final String deviceId);
}
