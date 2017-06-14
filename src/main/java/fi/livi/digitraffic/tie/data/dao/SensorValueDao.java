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

import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.lotju.xsd.lam.Lam;
import fi.livi.digitraffic.tie.lotju.xsd.tiesaa.Tiesaa;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;

@Repository
public class SensorValueDao {

    private static final Logger log = LoggerFactory.getLogger(SensorValueDao.class);

    private static final String MERGE_STATEMENT =
        "MERGE INTO SENSOR_VALUE dst\n" +
            "USING (\n" +
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
            "       ) as road_station_id FROM DUAL\n" +
            ") src ON (dst.road_station_sensor_id = src.road_station_sensor_id\n" +
            "      AND dst.road_station_id = src.road_station_id)\n" +
            "WHEN MATCHED THEN UPDATE SET dst.value = :value\n" +
            "                           , dst.measured = :measured\n" +
            "                           , dst.updated = sysdate\n" +
            "                           , dst.time_window_start = :timeWindowStart\n" +
            "                           , dst.time_window_end = :timeWindowEnd\n" +
            "WHEN NOT MATCHED THEN INSERT (dst.id, dst.road_station_id, dst.road_station_sensor_id, dst.value, dst.measured, dst.updated, dst.time_window_start, dst.time_window_end)\n" +
            "     VALUES (SEQ_SENSOR_VALUE.nextval\n" +
            "           , src.road_station_id\n" +
            "           , src.road_station_sensor_id\n" +
            "           , :value\n" +
            "           , :measured\n" +
            "           , sysdate\n" + // updated
            "           , :timeWindowStart\n" +
            "           , :timeWindowEnd)\n" +
            "     WHERE src.road_station_id IS NOT NULL\n" +
            "       AND src.road_station_sensor_id IS NOT NULL";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public SensorValueDao(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int updateLamSensorData(Collection<Lam> data, Set<Long> allowedTmsSensorLotjuIds) {
        ArrayList<Map<String, Object>> batchData = appendLamBatchData(data, allowedTmsSensorLotjuIds);
        jdbcTemplate.batchUpdate(MERGE_STATEMENT, batchData.toArray(new Map[0]));
        return batchData.size();
    }

    public int updateWeatherSensorData(Collection<Tiesaa> data, Set<Long> allowedWeatherSensorLotjuIds) {
        Map<String, Object>[] batchData = appendTiesaaBatchData(data, allowedWeatherSensorLotjuIds);
        jdbcTemplate.batchUpdate(MERGE_STATEMENT, batchData);
        return batchData.length;
    }

    private static ArrayList<Map<String, Object>> appendLamBatchData(final Collection<Lam> lams,
                                                                     final Set<Long> allowedTmsSensorLotjuIds) {
        final ArrayList<Map<String, Object>> batchData = new ArrayList<>();
        int updateCount = 0;
        int notAllowed = 0;
        for (Lam lam : lams) {
            final List<Lam.Anturit.Anturi> anturit = lam.getAnturit().getAnturi();
            for (Lam.Anturit.Anturi anturi : anturit) {
                if ( allowedTmsSensorLotjuIds.contains( Long.parseLong(anturi.getLaskennallinenAnturiId()) ) ) {
                    batchData.add(createArgsMap(lam, anturi));
                    updateCount++;
                } else {
                    notAllowed++;
                }
            }
        }
        log.info("Update {} allowed and skipped {} not allowed tms sensor values", updateCount, notAllowed);
        return batchData;
    }

    private static Map<String, Object>[] appendTiesaaBatchData(final Collection<Tiesaa> tiesaas,
                                                               final Set<Long> allowedWeatherSensorLotjuIds) {
        int updateCount = 0;
        int notAllowed = 0;
        final ArrayList<Map> batchData = new ArrayList<>();
        for (Tiesaa tiesaa : tiesaas) {
            final List<Tiesaa.Anturit.Anturi> anturit = tiesaa.getAnturit().getAnturi();
            for (Tiesaa.Anturit.Anturi anturi : anturit) {
                if (allowedWeatherSensorLotjuIds.contains(anturi.getLaskennallinenAnturiId())) {
                    batchData.add(createArgsMap(tiesaa, anturi));
                    updateCount++;

                } else {
                    notAllowed++;
                }
            }
        }
        log.info("Update {} allowed and skipped {} not allowed weather sensor values", updateCount, notAllowed);
        return (Map<String, Object>[])batchData.toArray(((Map[])new HashMap[0]));
    }

    private static Map createArgsMap(Lam lam, Lam.Anturit.Anturi anturi) {
        final LocalDateTime sensorValueMeasured = DateHelper.toLocalDateTime(lam.getAika());
        final Timestamp measured = Timestamp.valueOf(sensorValueMeasured);
        final HashMap<String, Object> args = new HashMap<>();

        args.put("value", (double) anturi.getArvo());
        args.put("measured", measured);
        args.put("rsLotjuId", lam.getAsemaId());
        args.put("sensorLotjuId", Long.parseLong(anturi.getLaskennallinenAnturiId()));
        args.put("stationType", RoadStationType.TMS_STATION.name());
        final LocalDateTime alku = DateHelper.toLocalDateTime(anturi.getAikaikkunaAlku());
        final LocalDateTime loppu = DateHelper.toLocalDateTime(anturi.getAikaikkunaLoppu());
        args.put("timeWindowStart", alku != null ? Timestamp.valueOf(alku) : null);
        args.put("timeWindowEnd", loppu != null ? Timestamp.valueOf(loppu) : null);

        return args;
    }

    private static Map createArgsMap(Tiesaa tiesaa, Tiesaa.Anturit.Anturi anturi) {
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