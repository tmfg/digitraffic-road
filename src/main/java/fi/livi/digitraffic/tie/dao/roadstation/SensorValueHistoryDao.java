package fi.livi.digitraffic.tie.dao.roadstation;

import java.sql.JDBCType;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.v1.SensorValueUpdateParameterDto;

@Repository
public class SensorValueHistoryDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String INSERT = """
            INSERT INTO SENSOR_VALUE_HISTORY(id, road_station_id, road_station_sensor_id, value,
                                     measured, reliability)
            SELECT nextval('seq_sensor_value_history'), :roadStationId, sensor.id,
                   :value, :measured, :reliability
            FROM ROAD_STATION_SENSOR sensor
            WHERE sensor.lotju_id = :sensorLotjuId
              AND sensor.road_station_type = :stationType
              AND sensor.publishable = true""";

    private static final String CLEAN = "DELETE FROM SENSOR_VALUE_HISTORY WHERE measured < :remove_before";

    @Autowired
    public SensorValueHistoryDao(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * @param params
     * @return the number of rows inserted for each param.
     */
    public int[] insertSensorData(final List<SensorValueUpdateParameterDto> params) {
        final MapSqlParameterSource[] batchData = getMapSqlParameterSources(params);

        return jdbcTemplate.batchUpdate(INSERT, batchData);
    }

    private static MapSqlParameterSource[] getMapSqlParameterSources(final List<SensorValueUpdateParameterDto> params) {
        return params.stream().map(p -> new MapSqlParameterSource()
                        .addValue("value", p.getValue(), JDBCType.NUMERIC.getVendorTypeNumber())
                        .addValue("measured", p.getMeasured(), JDBCType.TIMESTAMP_WITH_TIMEZONE.getVendorTypeNumber())
                        .addValue("roadStationId", p.getRoadStationId(), JDBCType.NUMERIC.getVendorTypeNumber())
                        .addValue("sensorLotjuId", p.getSensorLotjuId(), JDBCType.NUMERIC.getVendorTypeNumber())
                        .addValue("stationType", p.getStationType(), JDBCType.VARCHAR.getVendorTypeNumber())
                        .addValue("reliability", p.getReliability(), JDBCType.VARCHAR.getVendorTypeNumber()))
                .toArray(MapSqlParameterSource[]::new);
    }

    public int cleanSensorData(final Instant time) {
        final MapSqlParameterSource source = new MapSqlParameterSource();

        // this needs to be OffsetDateTime
        source.addValue("remove_before", OffsetDateTime.ofInstant(time, ZoneId.of("UTC")));

        return jdbcTemplate.update(CLEAN, source);
    }
}
