package fi.livi.digitraffic.tie.data.service;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.lotju.xsd.lam.Lam;
import fi.livi.digitraffic.tie.lotju.xsd.tiesaa.Tiesaa;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.internal.OracleConnection;

@Service
public class SensorDataUpdateService {
    private static final Logger log = LoggerFactory.getLogger(SensorDataUpdateService.class);

    private final DataSource dataSource;

    private static final String INSERT_STATEMENT =
            "INSERT INTO SENSOR_VALUE \n" +
            "  (id, road_station_id, road_station_sensor_id, value, measured, updated)\n" +
            "       ( SELECT SEQ_SENSOR_VALUE.nextval AS id\n" +
            "              , station.id AS road_station_id\n" +
            "              , sensor.id AS road_station_sensor_id\n" +
            "              , :value AS value\n" +
            "              , :measured AS measured\n" +
            "              , sysdate AS updated\n" +
            "         FROM ROAD_STATION_SENSOR sensor\n" +
            "            , ROAD_STATION station\n" +
            "         WHERE station.lotju_id = :rsLotjuId\n" +
            "           AND station.road_station_type = :stationType\n" +
            "           AND sensor.lotju_id = :sensorLotjuId\n" +
            "           AND sensor.road_station_type = :stationType\n" +
            "           AND NOT EXISTS (\n" +
            "                   SELECT NULL\n" +
            "                   FROM SENSOR_VALUE sv\n" +
            "                   WHERE sv.road_station_sensor_id = sensor.id\n" +
            "                     AND sv.road_station_id = station.id\n" +
            "               )\n" +
            "       )";

    private static final String UPDATE_STATEMENT =
            "UPDATE ( SELECT station.id AS stationId\n" +
            "              , sensor.id AS sensorId\n" +
            "              , :value AS value\n" +
            "              , :measured AS sensorValueMeasured\n" +
            "              , dst.road_station_sensor_id dst_rss_id\n" +
            "              , dst.road_station_id dst_rs_id\n" +
            "              , dst.value dst_value\n" +
            "              , dst.measured dst_measured\n" +
            "              , dst.updated dst_updated\n" +
            "         FROM ROAD_STATION_SENSOR sensor\n" +
            "            , ROAD_STATION station\n" +
            "            , SENSOR_VALUE dst\n" +
            "         WHERE station.lotju_id = :rsLotjuId\n" +
            "           AND station.road_station_type = :stationType\n" +
            "           AND sensor.lotju_id = :sensorLotjuId\n" +
            "           AND sensor.road_station_type = :stationType\n" +
            "           AND dst.road_station_sensor_id = sensor.id\n" +
            "           AND dst.road_station_id = station.id )\n" +
            "SET dst_value = value\n" +
            "  , dst_measured = sensorValueMeasured\n" +
            "  , dst_updated = sysdate";

    @Autowired
    public SensorDataUpdateService(final DataSource dataSource) throws SQLException {
        this.dataSource = dataSource;
    }

    /**
     * Updates weather data to db
     * @param data
     * @return true if success
     */
    @Transactional
    public boolean updateLamData(List<Lam> data) {

        try (OracleConnection connection = (OracleConnection) dataSource.getConnection();
             OraclePreparedStatement opsUpdate = (OraclePreparedStatement) connection.prepareStatement(UPDATE_STATEMENT);
             OraclePreparedStatement opsInsert = (OraclePreparedStatement) connection.prepareStatement(INSERT_STATEMENT)) {

            final long startFilter = System.currentTimeMillis();
            final Collection<Lam> newestLamData = filterNewestLamValues(data);
            final long endFilterStartAppend = System.currentTimeMillis();
            final int rows = appendLamBatchData(opsUpdate, newestLamData);
            appendLamBatchData(opsInsert, newestLamData);

            final long endAppendStartUpdate = System.currentTimeMillis();
            opsUpdate.executeBatch();
            final long endUpdateStartInsert = System.currentTimeMillis();
            opsInsert.executeBatch();
            final long endInsert = System.currentTimeMillis();

            log.info(String.format("Update lam sensors data for %1$d " +
                                   "rows, took %2$d ms (data filter: %3$d ms, " +
                                   "append batch: %4$d ms, update %5$d ms, insert: %6$d ms)",
                                   rows,
                                   endInsert - startFilter, // total
                                   endFilterStartAppend - startFilter, // filter data
                                   endAppendStartUpdate - endFilterStartAppend, // append data
                                   endUpdateStartInsert - endAppendStartUpdate, // update
                                   endInsert-endUpdateStartInsert)); // insert
            return true;
        } catch (Exception e) {
            log.error("Error while updating lam data", e);
        }
        return false;
    }

    /**
     * Updates weather data to db
     * @param data
     * @return true if success
     */
    @Transactional
    public boolean updateWeatherData(List<Tiesaa> data) {

        try (OracleConnection connection = (OracleConnection) dataSource.getConnection();
             OraclePreparedStatement opsUpdate = (OraclePreparedStatement) connection.prepareStatement(UPDATE_STATEMENT);
             OraclePreparedStatement opsInsert = (OraclePreparedStatement) connection.prepareStatement(INSERT_STATEMENT)) {

            final long startFilter = System.currentTimeMillis();
            final Collection<Tiesaa> newestTiesaaData = filterNewestTiesaaValues(data);
            final long endFilterStartAppend = System.currentTimeMillis();
            final int rows = appendTiesaaBatchData(opsUpdate, newestTiesaaData);
            appendTiesaaBatchData(opsInsert, newestTiesaaData);

            final long endAppendStartUpdate = System.currentTimeMillis();
            opsUpdate.executeBatch();
            final long endUpdateStartInsert = System.currentTimeMillis();
            opsInsert.executeBatch();
            final long endInsert = System.currentTimeMillis();

            log.info(String.format("Update weather sensors data for %1$d " +
                                   "rows, took %2$d ms (data filter: %3$d ms, " +
                                   "append batch: %4$d ms, update %5$d ms, insert: %6$d ms)",
                                   rows,
                                   endInsert - startFilter, // total
                                   endFilterStartAppend - startFilter, // filter data
                                   endAppendStartUpdate - endFilterStartAppend, // append data
                                   endUpdateStartInsert - endAppendStartUpdate, // update
                                   endInsert-endUpdateStartInsert)); // insert
            return true;
        } catch (Exception e) {
            log.error("Error while updating weather data", e);
        }
        return false;
    }

    private static Collection<Lam> filterNewestLamValues(List<Lam> data) {
        // Collect newest data per station
        HashMap<Long, Lam> lamMapByLamStationLotjuId = new HashMap<Long, Lam>();
        for (Lam lam : data) {
            Lam currentLam = lamMapByLamStationLotjuId.get(lam.getAsemaId());
            if (currentLam == null || lam.getAika().toGregorianCalendar().before(currentLam.getAika().toGregorianCalendar())) {
                if (currentLam != null) {
                    log.info("Replace " + currentLam.getAika() + " with " + lam.getAika());
                }
                lamMapByLamStationLotjuId.put(lam.getAsemaId(), lam);
            }
        }
        return lamMapByLamStationLotjuId.values();
    }

    private static Collection<Tiesaa> filterNewestTiesaaValues(List<Tiesaa> data) {
        // Collect newest data per station
        HashMap<Long, Tiesaa> tiesaaMapByLamStationLotjuId = new HashMap<>();
        for (Tiesaa tiesaa : data) {
            Tiesaa currentTiesaa = tiesaaMapByLamStationLotjuId.get(tiesaa.getAsemaId());
            if (currentTiesaa == null || tiesaa.getAika().toGregorianCalendar().before(currentTiesaa.getAika().toGregorianCalendar())) {
                if (currentTiesaa != null) {
                    log.info("Replace " + currentTiesaa.getAika() + " with " + tiesaa.getAika());
                }
                tiesaaMapByLamStationLotjuId.put(tiesaa.getAsemaId(), tiesaa);
            }
        }
        return tiesaaMapByLamStationLotjuId.values();
    }


    private static int appendLamBatchData(OraclePreparedStatement ops, Collection<Lam> lams) throws SQLException {
        int rows = 0;

        for (Lam lam : lams) {
            List<Lam.Anturit.Anturi> anturit = lam.getAnturit().getAnturi();
            LocalDateTime sensorValueMeasured = DateHelper.toLocalDateTimeAtZone(lam.getAika(), ZoneId.systemDefault());
            Timestamp measured = Timestamp.valueOf(sensorValueMeasured);
            for (Lam.Anturit.Anturi anturi : anturit) {
                rows++;
                ops.setDoubleAtName("value", (double) anturi.getArvo());
                ops.setTimestampAtName("measured", measured);
                ops.setLongAtName("rsLotjuId", lam.getAsemaId());
                ops.setLongAtName("sensorLotjuId", Long.parseLong(anturi.getLaskennallinenAnturiId()));
                ops.setStringAtName("stationType", RoadStationType.LAM_STATION.name());
                ops.addBatch();
            }
        }
        return rows;
    }

    private static int appendTiesaaBatchData(OraclePreparedStatement ops, Collection<Tiesaa> tiesaas) throws SQLException {
        int rows = 0;

        for (Tiesaa tiesaa : tiesaas) {
            List<Tiesaa.Anturit.Anturi> anturit = tiesaa.getAnturit().getAnturi();
            LocalDateTime sensorValueMeasured = DateHelper.toLocalDateTimeAtZone(tiesaa.getAika(), ZoneId.systemDefault());
            Timestamp measured = Timestamp.valueOf(sensorValueMeasured);
            for (Tiesaa.Anturit.Anturi anturi : anturit) {
                rows++;
                ops.setDoubleAtName("value", (double) anturi.getArvo());
                ops.setTimestampAtName("measured", measured);
                ops.setLongAtName("rsLotjuId", tiesaa.getAsemaId());
                ops.setLongAtName("sensorLotjuId", anturi.getLaskennallinenAnturiId());
                ops.setStringAtName("stationType", RoadStationType.WEATHER_STATION.name());
                ops.addBatch();
            }
        }
        return rows;
    }
}
