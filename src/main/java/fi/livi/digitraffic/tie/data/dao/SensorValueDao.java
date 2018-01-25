package fi.livi.digitraffic.tie.data.dao;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import fi.ely.lotju.lam.proto.LAMRealtimeProtos;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.TimestampCache;
import fi.livi.digitraffic.tie.lotju.xsd.tiesaa.Tiesaa;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;

@Repository
public class SensorValueDao {
    private static final Logger log = LoggerFactory.getLogger(SensorValueDao.class);

    private static final String MERGE_STATEMENT = "with upsert as (\n" +
        "  SELECT (\n" +
        "        SELECT sensor.id  \n" +
        "        FROM ROAD_STATION_SENSOR sensor\n" +
        "        WHERE sensor.lotju_id = :sensorLotjuId\n" +
        "          AND sensor.road_station_type = :stationType\n" +
        "          AND sensor.obsolete_date is null\n" +
        "        ) as road_station_sensor_id ,(\n" +
        "        SELECT station.id\n" +
        "        FROM ROAD_STATION station\n" +
        "        WHERE station.lotju_id = :rsLotjuId\n" +
        "          AND station.road_station_type = :stationType\n" +
        "          AND station.obsolete_date is null\n" +
        "       ) as road_station_id\n" +
        ")\n" +
        "insert into sensor_value(id, road_station_id, road_station_sensor_id, " +
        "value, measured, updated, time_window_start, time_window_end)\n" +
        "select nextval('seq_sensor_value'), upsert.road_station_id, " +
        "upsert.road_station_sensor_id, :value, :measured, current_timestamp, :timeWindowStart, :timeWindowEnd from upsert\n" +
        "on " +
        "conflict (road_station_id, road_station_sensor_id)\n" +
        "do update set value = :value\n" +
        "                , measured = :measured\n" +
        "                , updated = current_timestamp\n" +
        "                , time_window_start = :timeWindowStart\n" +
        "                , time_window_end = :timeWindowEnd\n" +
        "WHERE (sensor_value.value != :value\n" +
        "     OR sensor_value.time_window_start is null\n" +
        "     OR sensor_value.time_window_start != :timeWindowStart)\n" +
        "     AND sensor_value.measured < :measured";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public SensorValueDao(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int updateLamSensorData(final Collection<LAMRealtimeProtos.Lam> data, final Set<Long> allowedTmsSensorLotjuIds) {
        final TimestampCache timestampCache = new TimestampCache();
        final ArrayList<Map<String, Object>> batchData = appendLamBatchData(timestampCache, data, allowedTmsSensorLotjuIds);

        return batchUpdate(MERGE_STATEMENT, batchData.toArray(new Map[0]));
    }

    public int updateWeatherSensorData(final Collection<Tiesaa> data, final Set<Long> allowedWeatherSensorLotjuIds) {
        final Map<String, Object>[] batchData = appendTiesaaBatchData(data, allowedWeatherSensorLotjuIds);

        return batchUpdate(MERGE_STATEMENT, batchData);
    }

    private int batchUpdate(final String statement, final Map<String, Object> data[]) {
        jdbcTemplate.batchUpdate(statement, data);

        return data.length;
    }

    private static ArrayList<Map<String, Object>> appendLamBatchData(final TimestampCache timestampCache, final Collection<LAMRealtimeProtos.Lam> lams,
        final Set<Long> allowedTmsSensorLotjuIds) {
        final ArrayList<Map<String, Object>> batchData = new ArrayList<>();
        int updateCount = 0;
        int notAllowed = 0;

        for (final LAMRealtimeProtos.Lam lam : lams) {
            final List<LAMRealtimeProtos.Lam.Anturi> anturit = lam.getAnturiList();
            for (final LAMRealtimeProtos.Lam.Anturi anturi : anturit) {
                if ( allowedTmsSensorLotjuIds.contains(anturi.getLaskennallinenAnturiId())) {
                    batchData.add(createArgsMap(timestampCache, lam, anturi));
                    updateCount++;
                } else {
                    notAllowed++;
                }
            }
        }
        log.info("updateCount={} allowed and skipped notAllowedCount={} not allowed tms sensor values", updateCount, notAllowed);
        return batchData;
    }

    private static Map<String, Object>[] appendTiesaaBatchData(final Collection<Tiesaa> tiesaas,
                                                               final Set<Long> allowedWeatherSensorLotjuIds) {
        int updateCount = 0;
        int notAllowed = 0;
        final ArrayList<Map> batchData = new ArrayList<>();
        for (final Tiesaa tiesaa : tiesaas) {
            final List<Tiesaa.Anturit.Anturi> anturit = tiesaa.getAnturit().getAnturi();
            for (final Tiesaa.Anturit.Anturi anturi : anturit) {
                if (allowedWeatherSensorLotjuIds.contains(anturi.getLaskennallinenAnturiId())) {
                    batchData.add(createArgsMap(tiesaa, anturi));
                    updateCount++;

                } else {
                    notAllowed++;
                }
            }
        }
        log.info("updateCount={} allowed and skipped notAllowedCount={} not allowed weather sensor values", updateCount, notAllowed);
        return (Map<String, Object>[])batchData.toArray(((Map[])new HashMap[0]));
    }

    private static Map createArgsMap(final TimestampCache timestampCache, final LAMRealtimeProtos.Lam lam, final LAMRealtimeProtos.Lam.Anturi anturi) {
        final HashMap<String, Object> args = new HashMap<>();

        args.put("value", anturi.getArvo());
        args.put("measured", timestampCache.get(lam.getAika()));
        args.put("rsLotjuId", lam.getAsemaId());
        args.put("sensorLotjuId", anturi.getLaskennallinenAnturiId());
        args.put("stationType", RoadStationType.TMS_STATION.name());
        args.put("timeWindowStart", anturi.hasAikaikkunaAlku() ? timestampCache.get(anturi.getAikaikkunaAlku()) : null);
        args.put("timeWindowEnd", anturi.hasAikaikkunaLoppu() ? timestampCache.get(anturi.getAikaikkunaLoppu()) : null);

        return args;
    }

    private static Map createArgsMap(final Tiesaa tiesaa, final Tiesaa.Anturit.Anturi anturi) {
        final LocalDateTime sensorValueMeasured = DateHelper.toLocalDateTime(tiesaa.getAika());
        final Timestamp measured = Timestamp.valueOf(sensorValueMeasured);
        final HashMap<String, Object> args = new HashMap<>();

        args.put("value", (double) anturi.getArvo());
        args.put("measured", measured);
        args.put("rsLotjuId", tiesaa.getAsemaId());
        args.put("sensorLotjuId", anturi.getLaskennallinenAnturiId());
        args.put("stationType", RoadStationType.WEATHER_STATION.name());
        args.put("timeWindowStart", null);
        args.put("timeWindowEnd", null);

        return args;
    }
}
