package fi.livi.digitraffic.tie.data.service;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.ely.lotju.lam.proto.LAMRealtimeProtos;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.lotju.xsd.lam.Lam;
import fi.livi.digitraffic.tie.lotju.xsd.tiesaa.Tiesaa;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.internal.OracleConnection;

@Service
public class SensorDataUpdateService {
    private static final Logger log = LoggerFactory.getLogger(SensorDataUpdateService.class);

    private final DataSource dataSource;
    private final Set<Long> allowedTmsSensorLotjuIds;
    private final Set<Long> allowedWeatherSensorLotjuIds;

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



    @Autowired
    public SensorDataUpdateService(final DataSource dataSource,
                                   final RoadStationSensorService roadStationSensorService) {
        this.dataSource = dataSource;

        final List<RoadStationSensor> allowedTmsSensors =
            roadStationSensorService.findAllNonObsoleteRoadStationSensors(RoadStationType.TMS_STATION);
        allowedTmsSensorLotjuIds = allowedTmsSensors.stream().map(s -> s.getLotjuId()).collect(Collectors.toSet());

        final List<RoadStationSensor> allowedWeatherSensors =
            roadStationSensorService.findAllNonObsoleteRoadStationSensors(RoadStationType.WEATHER_STATION);
        allowedWeatherSensorLotjuIds = allowedWeatherSensors.stream().map(s -> s.getLotjuId()).collect(Collectors.toSet());
    }

    /**
     * Updates tms sensors data to db
     * @param data
     * @return true if success
     */
    @Transactional
    public boolean updateLamData(final List<LAMRealtimeProtos.Lam> data) {
        try (final OracleConnection connection = (OracleConnection) dataSource.getConnection();
             final OraclePreparedStatement opsMerge = (OraclePreparedStatement) connection.prepareStatement(MERGE_STATEMENT)) {
            final StopWatch stopWatch = StopWatch.createStarted();
            final Collection<LAMRealtimeProtos.Lam> newestLamData = filterNewestLamValues(data);

            final Pair<Integer, Integer> rowsAndNotAllowed = appendLamBatchData(opsMerge, newestLamData, allowedTmsSensorLotjuIds);
            opsMerge.executeBatch();
            stopWatch.stop();

            log.info("Update tms sensors data for {} rows, took {} ms (Skipped {} not allowed sensor values)",
                     rowsAndNotAllowed.getLeft(),
                     stopWatch.getTime(),
                     rowsAndNotAllowed.getRight());
            return true;
        } catch (final Exception e) {
            log.error("Error while updating tms data", e);
        }
        return false;
    }

    /**
     * Updates weather data to db
     * @param data
     * @return true if success
     */
    @Transactional
    public boolean updateWeatherData(final List<Tiesaa> data) {
        try (final OracleConnection connection = (OracleConnection) dataSource.getConnection();
             final OraclePreparedStatement opsMerge = (OraclePreparedStatement) connection.prepareStatement(MERGE_STATEMENT)) {

            final StopWatch stopWatch = StopWatch.createStarted();
            final Collection<Tiesaa> newestTiesaaData = filterNewestTiesaaValues(data);
            final Pair<Integer, Integer> rowsAndNotAllowed =
                appendTiesaaBatchData(opsMerge, newestTiesaaData, allowedWeatherSensorLotjuIds);
            opsMerge.executeBatch();
            stopWatch.stop();

            log.info("Update weather sensors data for {} rows took {} ms (Skipped {} not allowed sensor values)",
                     rowsAndNotAllowed.getLeft(),
                     stopWatch.getTime(),
                     rowsAndNotAllowed.getRight());

            return true;
        } catch (final Exception e) {
            log.error("Error while updating weather data", e);
        }
        return false;
    }

    private static Collection<LAMRealtimeProtos.Lam> filterNewestLamValues(final List<LAMRealtimeProtos.Lam> data) {
        // Collect newest data per station
        final HashMap<Long, LAMRealtimeProtos.Lam> tmsMapByLamStationLotjuId = new HashMap<>();

        for (final LAMRealtimeProtos.Lam lam : data) {
            final LAMRealtimeProtos.Lam currentLam = tmsMapByLamStationLotjuId.get(lam.getAsemaId());
            if (currentLam == null || lam.getAika() < currentLam.getAika()) {
                if (currentLam != null) {
                    log.info("Replace " + currentLam.getAika() + " with " + lam.getAika());
                }
                tmsMapByLamStationLotjuId.put(lam.getAsemaId(), lam);
            }
        }
        return tmsMapByLamStationLotjuId.values();
    }

    private static Collection<Tiesaa> filterNewestTiesaaValues(final List<Tiesaa> data) {
        // Collect newest data per station
        final HashMap<Long, Tiesaa> tiesaaMapByTmsStationLotjuId = new HashMap<>();
        for (final Tiesaa tiesaa : data) {
            final Tiesaa currentTiesaa = tiesaaMapByTmsStationLotjuId.get(tiesaa.getAsemaId());
            if (currentTiesaa == null || tiesaa.getAika().toGregorianCalendar().before(currentTiesaa.getAika().toGregorianCalendar())) {
                if (currentTiesaa != null) {
                    log.info("Replace " + currentTiesaa.getAika() + " with " + tiesaa.getAika());
                }
                tiesaaMapByTmsStationLotjuId.put(tiesaa.getAsemaId(), tiesaa);
            }
        }
        return tiesaaMapByTmsStationLotjuId.values();
    }


    private static Pair<Integer, Integer> appendLamBatchData(final OraclePreparedStatement ops,
                                                             final Collection<LAMRealtimeProtos.Lam> lams,
                                                             final Set<Long> allowedTmsSensorLotjuIds) throws SQLException {
        int rows = 0;
        int notAllowed = 0;
        for (final LAMRealtimeProtos.Lam lam : lams) {
            final List<LAMRealtimeProtos.Lam.Anturi> anturit = lam.getAnturiList();
            final LocalDateTime sensorValueMeasured = DateHelper.toLocalDateTime(lam.getAika());
            final Timestamp measured = Timestamp.valueOf(sensorValueMeasured);

            for (final LAMRealtimeProtos.Lam.Anturi anturi : anturit) {
                final long sensorLotjuId = anturi.getLaskennallinenAnturiId();

                if (allowedTmsSensorLotjuIds.contains(sensorLotjuId)) {
                    rows++;
                    ops.setDoubleAtName("value", (double) anturi.getArvo());
                    ops.setTimestampAtName("measured", measured);
                    ops.setLongAtName("rsLotjuId", lam.getAsemaId());
                    ops.setLongAtName("sensorLotjuId", sensorLotjuId);
                    ops.setStringAtName("stationType", RoadStationType.TMS_STATION.name());
                    final LocalDateTime alku = DateHelper.toLocalDateTime(anturi.getAikaikkunaAlku());
                    final LocalDateTime loppu = DateHelper.toLocalDateTime(anturi.getAikaikkunaLoppu());
                    ops.setTimestampAtName("timeWindowStart", alku != null ? Timestamp.valueOf(alku) : null);
                    ops.setTimestampAtName("timeWindowEnd", loppu != null ? Timestamp.valueOf(loppu) : null);
                    ops.addBatch();
                } else {
                    notAllowed++;
                }
            }
        }
        return Pair.of(rows, notAllowed);
    }

    private static Pair<Integer, Integer> appendTiesaaBatchData(final OraclePreparedStatement ops,
                                                                final Collection<Tiesaa> tiesaas,
                                                                final Set<Long> allowedWeatherSensorLotjuIds) throws SQLException {
        int rows = 0;
        int notAllowed = 0;
        for (final Tiesaa tiesaa : tiesaas) {
            final List<Tiesaa.Anturit.Anturi> anturit = tiesaa.getAnturit().getAnturi();
            final LocalDateTime sensorValueMeasured = DateHelper.toLocalDateTime(tiesaa.getAika());
            final Timestamp measured = Timestamp.valueOf(sensorValueMeasured);

            for (final Tiesaa.Anturit.Anturi anturi : anturit) {
                final long sensorLotjuId = anturi.getLaskennallinenAnturiId();
                if (allowedWeatherSensorLotjuIds.contains(sensorLotjuId)) {
                    rows++;
                    ops.setDoubleAtName("value", (double) anturi.getArvo());
                    ops.setTimestampAtName("measured", measured);
                    ops.setLongAtName("rsLotjuId", tiesaa.getAsemaId());
                    ops.setLongAtName("sensorLotjuId", sensorLotjuId);
                    ops.setStringAtName("stationType", RoadStationType.WEATHER_STATION.name());
                    ops.setTimestampAtName("timeWindowStart", null);
                    ops.setTimestampAtName("timeWindowEnd", null);
                    ops.addBatch();
                } else {
                    notAllowed++;
                }
            }
        }
        return Pair.of(rows, notAllowed);
    }
}
