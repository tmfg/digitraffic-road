package fi.livi.digitraffic.tie.data.service;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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

    private static final String MERGE_STATEMENT =
            "MERGE INTO SENSOR_VALUE dst\n" +
                    "USING (\n" +
                    "  SELECT (\n" +
                    "        SELECT sensor.id  \n" +
                    "        FROM ROAD_STATION_SENSOR sensor\n" +
                    "        WHERE sensor.lotju_id = :sensorLotjuId\n" +
                    "          AND sensor.road_station_type = :stationType\n" +
                    "          AND sensor.lotju_id is not null\n" +
                    "          AND sensor.obsolete_date is null\n" +
                    "        ) as road_station_sensor_id ,(\n" +
                    "        SELECT station.id\n" +
                    "        FROM ROAD_STATION station\n" +
                    "        WHERE station.lotju_id = :rsLotjuId\n" +
                    "          AND station.road_station_type = :stationType\n" +
                    "          AND station.lotju_id is not null\n" +
                    "          AND station.obsolete_date is null\n" +
                    "       ) as road_station_id FROM DUAL\n" +
                    ") src ON (dst.road_station_sensor_id = src.road_station_sensor_id\n" +
                    "      AND dst.road_station_id = src.road_station_id)\n" +
                    "WHEN MATCHED THEN UPDATE SET dst.value = :value\n" +
                    "                           , dst.measured = :measured\n" +
                    "                           , dst.updated = sysdate\n" +
                    "WHEN NOT MATCHED THEN INSERT (dst.id, dst.road_station_id, dst.road_station_sensor_id, dst.value, dst.measured, dst.updated)\n" +
                    "     VALUES (SEQ_SENSOR_VALUE.nextval -- id\n" +
                    "           , src.road_station_id\n" +
                    "           , src.road_station_sensor_id\n" +
                    "           , :value\n" + // value
                    "           , :measured\n" + // measured
                    "           , sysdate)\n" + // updated
                    "     WHERE src.road_station_id IS NOT NULL\n" +
                    "       AND src.road_station_sensor_id IS NOT NULL";

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
             OraclePreparedStatement opsMerge = (OraclePreparedStatement) connection.prepareStatement(MERGE_STATEMENT)) {

            final long startFilter = System.currentTimeMillis();
            final Collection<Lam> newestLamData = filterNewestLamValues(data);
            final long endFilterStartAppend = System.currentTimeMillis();
            final int rows = appendLamBatchData(opsMerge, newestLamData);

            final long endAppendStartMerge = System.currentTimeMillis();
            opsMerge.executeBatch();
            final long endMerge = System.currentTimeMillis();

            log.info(String.format("Update tms sensors data for %1$d " +
                                   "rows, took %2$d ms (data filter: %3$d ms, " +
                                   "append batch: %4$d ms, merge %5$d ms)",
                                   rows,
                                   endMerge - startFilter, // total
                                   endFilterStartAppend - startFilter, // filter data
                                   endAppendStartMerge - endFilterStartAppend, // append data
                                   endMerge - endAppendStartMerge)); // merge
            return true;
        } catch (Exception e) {
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
    public boolean updateWeatherData(List<Tiesaa> data) {

        try (OracleConnection connection = (OracleConnection) dataSource.getConnection();
             OraclePreparedStatement opsMerge = (OraclePreparedStatement) connection.prepareStatement(MERGE_STATEMENT)) {

            final long startFilter = System.currentTimeMillis();
            final Collection<Tiesaa> newestTiesaaData = filterNewestTiesaaValues(data);
            final long endFilterStartAppend = System.currentTimeMillis();
            final int rows = appendTiesaaBatchData(opsMerge, newestTiesaaData);

            final long endAppendStartMerge = System.currentTimeMillis();
            opsMerge.executeBatch();
            final long endMerge = System.currentTimeMillis();

            log.info(String.format("Update weather sensors data for %1$d " +
                                   "rows, took %2$d ms (data filter: %3$d ms, " +
                                   "append batch: %4$d ms, merge %5$d ms)",
                                   rows,
                                   endMerge - startFilter, // total
                                   endFilterStartAppend - startFilter, // filter data
                                   endAppendStartMerge - endFilterStartAppend, // append data
                                   endMerge - endAppendStartMerge)); // merge
            return true;
        } catch (Exception e) {
            log.error("Error while updating weather data", e);
        }
        return false;
    }

    private static Collection<Lam> filterNewestLamValues(List<Lam> data) {
        // Collect newest data per station
        HashMap<Long, Lam> tmsMapByLamStationLotjuId = new HashMap<>();
        for (Lam lam : data) {
            Lam currentLam = tmsMapByLamStationLotjuId.get(lam.getAsemaId());
            if (currentLam == null || lam.getAika().toGregorianCalendar().before(currentLam.getAika().toGregorianCalendar())) {
                if (currentLam != null) {
                    log.info("Replace " + currentLam.getAika() + " with " + lam.getAika());
                }
                tmsMapByLamStationLotjuId.put(lam.getAsemaId(), lam);
            }
        }
        return tmsMapByLamStationLotjuId.values();
    }

    private static Collection<Tiesaa> filterNewestTiesaaValues(List<Tiesaa> data) {
        // Collect newest data per station
        HashMap<Long, Tiesaa> tiesaaMapByTmsStationLotjuId = new HashMap<>();
        for (Tiesaa tiesaa : data) {
            Tiesaa currentTiesaa = tiesaaMapByTmsStationLotjuId.get(tiesaa.getAsemaId());
            if (currentTiesaa == null || tiesaa.getAika().toGregorianCalendar().before(currentTiesaa.getAika().toGregorianCalendar())) {
                if (currentTiesaa != null) {
                    log.info("Replace " + currentTiesaa.getAika() + " with " + tiesaa.getAika());
                }
                tiesaaMapByTmsStationLotjuId.put(tiesaa.getAsemaId(), tiesaa);
            }
        }
        return tiesaaMapByTmsStationLotjuId.values();
    }


    private static int appendLamBatchData(OraclePreparedStatement ops, Collection<Lam> lams) throws SQLException {
        int rows = 0;

        for (Lam lam : lams) {
            List<Lam.Anturit.Anturi> anturit = lam.getAnturit().getAnturi();
            LocalDateTime sensorValueMeasured = DateHelper.toLocalDateTime(lam.getAika());
            Timestamp measured = Timestamp.valueOf(sensorValueMeasured);
            for (Lam.Anturit.Anturi anturi : anturit) {
                rows++;
                ops.setDoubleAtName("value", (double) anturi.getArvo());
                ops.setTimestampAtName("measured", measured);
                ops.setLongAtName("rsLotjuId", lam.getAsemaId());
                ops.setLongAtName("sensorLotjuId", Long.parseLong(anturi.getLaskennallinenAnturiId()));
                ops.setStringAtName("stationType", RoadStationType.TMS_STATION.name());
                ops.addBatch();
            }
        }
        return rows;
    }

    private static int appendTiesaaBatchData(OraclePreparedStatement ops, Collection<Tiesaa> tiesaas) throws SQLException {
        int rows = 0;

        for (Tiesaa tiesaa : tiesaas) {
            List<Tiesaa.Anturit.Anturi> anturit = tiesaa.getAnturit().getAnturi();
            LocalDateTime sensorValueMeasured = DateHelper.toLocalDateTime(tiesaa.getAika());
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
