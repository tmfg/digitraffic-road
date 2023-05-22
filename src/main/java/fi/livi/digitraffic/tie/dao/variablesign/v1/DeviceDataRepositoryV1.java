package fi.livi.digitraffic.tie.dao.variablesign.v1;

import java.time.Instant;
import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.variablesigns.v1.TrafficSignHistoryV1;
import fi.livi.digitraffic.tie.model.v2.variablesign.DeviceData;

@Repository
public interface DeviceDataRepositoryV1 extends JpaRepository<DeviceData, Long> {
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
    List<TrafficSignHistoryV1> getDeviceDataByDeviceIdOrderByEffectDateDesc(final String deviceId);

    @Query(value =
       "select max(modified)\n" +
       "from device_data", nativeQuery = true)
    Instant getLastUpdated();

    @Query(value =
       "select max(modified)\n" +
       "from device_data_datex2", nativeQuery = true)
    Instant getDatex2LastUpdated();
}
