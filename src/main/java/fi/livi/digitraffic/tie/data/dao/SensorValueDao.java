package fi.livi.digitraffic.tie.data.dao;

import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
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
        "SELECT nextval('seq_sensor_value'), :roadSationId, sensor.id,\n" +
        "       :value, :measured, current_timestamp, :timeWindowStart, :timeWindowEnd\n" +
        "FROM ROAD_STATION_SENSOR sensor\n" +
        "WHERE sensor.lotju_id = :sensorLotjuId\n" +
        "  AND sensor.road_station_type = :stationType\n" +
        "  AND sensor.publishable = true\n" +
        "on conflict (road_station_id, road_station_sensor_id)\n" +
        "do update set value = :value\n" +
        "                , measured = :measured\n" +
        "                , updated = current_timestamp\n" +
        "                , time_window_start = :timeWindowStart\n" +
        "                , time_window_end = :timeWindowEnd";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public SensorValueDao(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int updateLamSensorData(final List<SensorValueUpdateParameterDto> params) {

        final MapSqlParameterSource[] batchData = params.stream().map(p -> new MapSqlParameterSource()
            .addValue("value", p.getValue())
            .addValue("measured", p.getMeasured())
            .addValue("roadSationId", p.getRoadSationId())
            .addValue("sensorLotjuId", p.getSensorLotjuId())
            .addValue("stationType", p.getStationType())
            .addValue("timeWindowStart", p.getTimeWindowStart())
            .addValue("timeWindowEnd", p.getTimeWindowEnd()))
            .toArray(MapSqlParameterSource[]::new);

        return batchUpdate(MERGE_STATEMENT, batchData);
    }

    public int updateWeatherSensorData(final List<SensorValueUpdateParameterDto> params) {
        final MapSqlParameterSource[] batchData = params.stream().map(p -> new MapSqlParameterSource()
            .addValue("value", p.getValue())
            .addValue("measured", p.getMeasured())
            .addValue("roadSationId", p.getRoadSationId())
            .addValue("sensorLotjuId", p.getSensorLotjuId())
            .addValue("stationType", p.getStationType())
            .addValue("timeWindowStart", p.getTimeWindowStart())
            .addValue("timeWindowEnd", p.getTimeWindowEnd()))
            .toArray(MapSqlParameterSource[]::new);

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
                log.error("method=batchUpdate error={} in batch update", count[i]);
            }
        }

        log.info("method=batchUpdate Updated: {}", rowsUpdated);

        return rowsUpdated;
    }
}
