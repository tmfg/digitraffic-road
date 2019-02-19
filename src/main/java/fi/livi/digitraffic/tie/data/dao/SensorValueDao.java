package fi.livi.digitraffic.tie.data.dao;

import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import fi.ely.lotju.lam.proto.LAMRealtimeProtos;
import fi.ely.lotju.tiesaa.proto.TiesaaProtos;
import fi.livi.digitraffic.tie.helper.NumberConverter;
import fi.livi.digitraffic.tie.helper.TimestampCache;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;

@Repository
public class SensorValueDao {
    private static final Logger log = LoggerFactory.getLogger(SensorValueDao.class);

    private static final String MERGE_STATEMENT =
        "INSERT INTO sensor_value(id, road_station_id, road_station_sensor_id, value,\n" +
        "                         measured, updated, time_window_start, time_window_end)\n" +
        "SELECT nextval('seq_sensor_value'), :rsId, sensor.id,\n" +
        "       :value, :measured, current_timestamp, :timeWindowStart, :timeWindowEnd\n" +
        "FROM ROAD_STATION_SENSOR sensor\n" +
        "WHERE sensor.lotju_id = :sensorLotjuId\n" +
        "  AND sensor.road_station_type = :stationType\n" +
        "  AND sensor.obsolete_date is null\n" +
        "on conflict (road_station_id, road_station_sensor_id)\n" +
        "do update set value = :value\n" +
        "                , measured = :measured\n" +
        "                , updated = current_timestamp\n" +
        "                , time_window_start = :timeWindowStart\n" +
        "                , time_window_end = :timeWindowEnd\n";
//        "WHERE sensor_value.measured < :measured\n" +
//        "     AND (sensor_value.value != :value\n" +
//        "       OR sensor_value.time_window_start is null\n" +
//        "       OR sensor_value.time_window_start != :timeWindowStart)";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public SensorValueDao(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int updateLamSensorData(final Collection<LAMRealtimeProtos.Lam> data,
                                   final Set<Long> allowedTmsSensorLotjuIds,
                                   final Map<Long, Long> allowedStationsLotjuIdtoIds) {
        final TimestampCache timestampCache = new TimestampCache();
        final MapSqlParameterSource[] batchData = createLamBatchData(timestampCache, data, allowedTmsSensorLotjuIds, allowedStationsLotjuIdtoIds);

        return batchUpdate(MERGE_STATEMENT, batchData);
    }

    public int updateWeatherSensorData(final Collection<TiesaaProtos.TiesaaMittatieto> data,
                                       final Set<Long> allowedWeatherSensorLotjuIds,
                                       final Map<Long, Long> allowedStationsLotjuIdtoIds) {
        final MapSqlParameterSource[] batchData = createTiesaaBatchData(data, allowedWeatherSensorLotjuIds, allowedStationsLotjuIdtoIds);

        return batchUpdate(MERGE_STATEMENT, batchData);
    }

    private int batchUpdate(final String statement, final MapSqlParameterSource data[]) {
        int[] count = jdbcTemplate.batchUpdate(statement, data);
        int rowsUpdated = 0;
        for (int i : count) {
            if (count[i] >= 0) {
                rowsUpdated += count[i];
            } else if (count[i] == Statement.SUCCESS_NO_INFO ){
                log.info("method=batchUpdate Could not resolve update count");
            } else {
                log.error("method=batchUpdate Error {} in batch update", count[i]);
            }
        }

        log.info("method=batchUpdate Updated: {}", rowsUpdated);

        return rowsUpdated;
    }

    private static MapSqlParameterSource[] createLamBatchData(final TimestampCache timestampCache,
                                                              final Collection<LAMRealtimeProtos.Lam> lams,
                                                              final Set<Long> allowedTmsSensorLotjuIds,
                                                              final Map<Long, Long> allowedStationsLotjuIdtoIds) {

        final ArrayList<MapSqlParameterSource> batchData = new ArrayList<>();
        int updateCount = 0;
        int notAllowed = 0;
        int skippedStations = 0;
        for (final LAMRealtimeProtos.Lam lam : lams) {
            if (allowedStationsLotjuIdtoIds.containsKey(lam.getAsemaId())) {
                final List<LAMRealtimeProtos.Lam.Anturi> anturit = lam.getAnturiList();
                for (final LAMRealtimeProtos.Lam.Anturi anturi : anturit) {
                    if (allowedTmsSensorLotjuIds.contains(anturi.getLaskennallinenAnturiId())) {
                        batchData.add(
                            new MapSqlParameterSource()
                                .addValue("value", anturi.getArvo())
                                .addValue("measured", timestampCache.get(lam.getAika()))
                                .addValue("rsId", allowedStationsLotjuIdtoIds.get(lam.getAsemaId()))
                                .addValue("sensorLotjuId", anturi.getLaskennallinenAnturiId())
                                .addValue("stationType", RoadStationType.TMS_STATION.name())
                                .addValue("timeWindowStart", anturi.hasAikaikkunaAlku() ? timestampCache.get(anturi.getAikaikkunaAlku()) : null)
                                .addValue("timeWindowEnd", anturi.hasAikaikkunaLoppu() ? timestampCache.get(anturi.getAikaikkunaLoppu()) : null));
                        updateCount++;
                    } else {
                        notAllowed++;
                    }
                }
            } else {
                skippedStations++;
                log.warn("method=createLamBatchData Skipped non existing or non publishable TmsStation with lotjuId {}", lam.getAsemaId());
            }
        }
        log.info("method=createLamBatchData updateCount={} allowed and skipped notAllowedCount={} not allowed tms sensor values and skippedStations={}",
                 updateCount, notAllowed, skippedStations);
        return batchData.toArray(new MapSqlParameterSource[0]);
    }

    private static MapSqlParameterSource[] createTiesaaBatchData(final Collection<TiesaaProtos.TiesaaMittatieto> tiesaas,
                                                                 final Set<Long> allowedWeatherSensorLotjuIds,
                                                                 final Map<Long, Long> allowedStationsLotjuIdtoIds) {
        int updateCount = 0;
        int notAllowed = 0;
        int skippedStations = 0;
        final ArrayList<MapSqlParameterSource> batchData = new ArrayList<>();

        for (final TiesaaProtos.TiesaaMittatieto tiesaa : tiesaas) {
            if (allowedStationsLotjuIdtoIds.containsKey(tiesaa.getAsemaId())) {
                final List<TiesaaProtos.TiesaaMittatieto.Anturi> anturit = tiesaa.getAnturiList();

                for (final TiesaaProtos.TiesaaMittatieto.Anturi anturi : anturit) {
                    if (allowedWeatherSensorLotjuIds.contains(anturi.getLaskennallinenAnturiId())) {
                        final Timestamp measured = Timestamp.from(Instant.ofEpochMilli(tiesaa.getAika()));
                        batchData.add(new MapSqlParameterSource()
                            .addValue("value", NumberConverter.convertAnturiValueToDouble(anturi.getArvo()))
                            .addValue("measured", measured)
                            .addValue("rsId", allowedStationsLotjuIdtoIds.get(tiesaa.getAsemaId()))
                            .addValue("sensorLotjuId", anturi.getLaskennallinenAnturiId())
                            .addValue("stationType", RoadStationType.WEATHER_STATION.name())
                            .addValue("timeWindowStart", null)
                            .addValue("timeWindowEnd", null));
                        updateCount++;

                    } else {
                        notAllowed++;
                    }
                }
            } else {
                skippedStations++;
                log.warn("method=createTiesaaBatchData Skipped non existing or non publishable WeatherStation with lotjuId {}", tiesaa.getAsemaId());
            }
        }
        log.info("method=createTiesaaBatchData updateCount={} allowed and skipped notAllowedCount={} not allowed weather sensor values and skippedStations={}",
                 updateCount, notAllowed, skippedStations);
        return batchData.toArray(new MapSqlParameterSource[0]);
    }
}
