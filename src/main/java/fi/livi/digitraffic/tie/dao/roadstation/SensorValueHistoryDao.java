package fi.livi.digitraffic.tie.dao.roadstation;

import java.sql.JDBCType;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;

import org.hibernate.type.SqlTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.v1.SensorValueUpdateParameterDto;

@Repository
public class SensorValueHistoryDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String INSERT = "INSERT INTO SENSOR_VALUE_HISTORY (id, road_station_id, road_station_sensor_id, value, measured, reliability) " +
        "VALUES (nextval('seq_sensor_value_history'), :road_station_id, :road_station_sensor_id, :value, :measured, :reliability)";

    private static final String CLEAN = "DELETE FROM SENSOR_VALUE_HISTORY WHERE measured < :remove_before";

    @Autowired
    public SensorValueHistoryDao(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * @param params sensor values
     * @return the number of rows inserted for each param.
     */
    public int[] insertSensorData(final List<SensorValueUpdateParameterDto> params) {
        final MapSqlParameterSource[] batchData = params.stream().map(p -> new MapSqlParameterSource()
            .addValue("value", p.getValue(), JDBCType.NUMERIC.getVendorTypeNumber())
            .addValue("measured", p.getMeasured(), JDBCType.TIMESTAMP_WITH_TIMEZONE.getVendorTypeNumber())
            .addValue("road_station_id", p.getRoadStationId(), JDBCType.NUMERIC.getVendorTypeNumber())
            .addValue("road_station_sensor_id", p.getSensorLotjuId(), JDBCType.NUMERIC.getVendorTypeNumber())
            .addValue("timeWindowStart", p.getTimeWindowStart(), JDBCType.TIMESTAMP_WITH_TIMEZONE.getVendorTypeNumber())
            .addValue("timeWindowEnd", p.getTimeWindowEnd(), JDBCType.TIMESTAMP_WITH_TIMEZONE.getVendorTypeNumber())
            .addValue("reliability", p.getReliability(), JDBCType.VARCHAR.getVendorTypeNumber()))
            .toArray(MapSqlParameterSource[]::new);

        return jdbcTemplate.batchUpdate(INSERT, batchData);
    }

    public int cleanSensorData(final Instant time) {
        final MapSqlParameterSource source = new MapSqlParameterSource();

        // this needs to be OffsetDateTime
        source.addValue("remove_before", OffsetDateTime.ofInstant(time, ZoneId.of("UTC")));

        return jdbcTemplate.update(CLEAN, source);
    }
}
