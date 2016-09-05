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
public class SensorDataUpdateServiceImpl implements SensorDataUpdateService {
    private static final Logger log = LoggerFactory.getLogger(SensorDataUpdateServiceImpl.class);

    private final OracleConnection connection;

    private static final String MERGE_STATEMENT =
            "MERGE INTO SENSOR_VALUE dst\n" +
            "  USING (SELECT station.id AS stationId\n" +
            "              , sensor.id AS sensorId\n" +
            "              , :value AS value\n" +
            "              , :measured AS sensorValueMeasured\n" +
            "         FROM ROAD_STATION_SENSOR sensor\n" +
            "            , ROAD_STATION station\n" +
            "         WHERE station.lotju_id = :rsLotjuId\n" +
            "           AND station.road_station_type = :stationType\n" +
            "           AND sensor.lotju_id = :sensorLotjuId\n" +
            "           AND sensor.road_station_type = :stationType) src\n" +
            "    ON (dst.road_station_sensor_id = src.sensorId AND dst.road_station_id = src.stationId)\n" +
            "  WHEN MATCHED THEN UPDATE\n" +
            "    SET dst.value = src.value, dst.measured = src.sensorValueMeasured\n" +
            "  WHEN NOT MATCHED THEN INSERT \n" +
            "    (dst.id, dst.road_station_id, dst.road_station_sensor_id, dst.value, dst.measured)\n" +
            "    VALUES (SEQ_SENSOR_VALUE.nextval, src.stationId, src.sensorId, src.value, src.sensorValueMeasured)";

    @Autowired
    public SensorDataUpdateServiceImpl(final DataSource dataSource) throws SQLException {
        this.connection = (OracleConnection)dataSource.getConnection();

    }

    @Transactional
    public void updateLamData(List<Lam> data) throws SQLException {

        final OraclePreparedStatement ops =
                (OraclePreparedStatement) connection.prepareStatement(MERGE_STATEMENT);

        final long startFilter = System.currentTimeMillis();
        final Collection<Lam> newestLamData = filterNewestLamValues(data);
        final long endFilterStartAppend = System.currentTimeMillis();
        final int rows = appendLamBatchData(ops, newestLamData);
        final long endAppendStartBatch = System.currentTimeMillis();
        ops.executeBatch();
        final long endBatch = System.currentTimeMillis();

        log.info(String.format("Update lam sensors data for " + rows + " rows took %1$d ms (data filter: %2$d ms, append batch: %3$d ms, merge: %4$d ms)",
                (endBatch - startFilter), (endFilterStartAppend - startFilter), (endAppendStartBatch - endFilterStartAppend),
                (endBatch - endAppendStartBatch)));
    }

    @Override
    public void updateWeatherData(List<Tiesaa> data) throws SQLException {
        final OraclePreparedStatement ops =
                (OraclePreparedStatement) connection.prepareStatement(MERGE_STATEMENT);

        final long startFilter = System.currentTimeMillis();
        final Collection<Tiesaa> newestTiesaaData = filterNewestTiesaaValues(data);
        final long endFilterStartAppend = System.currentTimeMillis();
        final int rows = appendTiesaaBatchData(ops, newestTiesaaData);
        final long endAppendStartBatch = System.currentTimeMillis();
        ops.executeBatch();
        final long endBatch = System.currentTimeMillis();

        log.info(String.format("Update weather sensors data for " + rows + " rows took %1$d ms (data filter: %2$d ms, append batch: %3$d ms, merge: %4$d ms)",
                (endBatch - startFilter), (endFilterStartAppend - startFilter), (endAppendStartBatch - endFilterStartAppend),
                (endBatch - endAppendStartBatch)));

    }

    private Collection<Lam> filterNewestLamValues(List<Lam> data) {
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

    private Collection<Tiesaa> filterNewestTiesaaValues(List<Tiesaa> data) {
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


    private int appendLamBatchData(OraclePreparedStatement ops, Collection<Lam> lams) throws SQLException {
        int rows = 0;

        for (Lam lam : lams) {
            List<Lam.Anturit.Anturi> anturit = lam.getAnturit().getAnturi();

            LocalDateTime sensorValueMeasured = DateHelper.toLocalDateTimeAtZone(lam.getAika(), ZoneId.systemDefault());
            Timestamp measured = Timestamp.valueOf(sensorValueMeasured);
            for (Lam.Anturit.Anturi anturi : anturit) {

                Object[] arr = { anturi.getLaskennallinenAnturiId(), lam.getAsemaId(), anturi.getArvo(), measured };
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

    private int appendTiesaaBatchData(OraclePreparedStatement ops, Collection<Tiesaa> tiesaas) throws SQLException {
        int rows = 0;

        for (Tiesaa tiesaa : tiesaas) {
            List<Tiesaa.Anturit.Anturi> anturit = tiesaa.getAnturit().getAnturi();

            LocalDateTime sensorValueMeasured = DateHelper.toLocalDateTimeAtZone(tiesaa.getAika(), ZoneId.systemDefault());
            Timestamp measured = Timestamp.valueOf(sensorValueMeasured);
            for (Tiesaa.Anturit.Anturi anturi : anturit) {

                Object[] arr = { anturi.getLaskennallinenAnturiId(), tiesaa.getAsemaId(), anturi.getArvo(), measured };
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
