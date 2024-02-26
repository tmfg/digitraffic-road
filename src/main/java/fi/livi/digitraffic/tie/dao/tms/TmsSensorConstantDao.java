package fi.livi.digitraffic.tie.dao.tms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioArvoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioVO;

@Repository
public class TmsSensorConstantDao {

    private static final Logger log = LoggerFactory.getLogger(TmsSensorConstantDao.class);

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final static String UPSERT_CONSTANTS_STATEMENT = """
        WITH TMS_RS AS (
            SELECT rs.id as rs_id, rs.lotju_id rs_lotju_id
            FROM road_station rs
            WHERE rs.road_station_type = 'TMS_STATION'
              AND rs.lotju_id = :sensorConstantRoadStationLotjuId
        )
        INSERT INTO TMS_SENSOR_CONSTANT (LOTJU_ID, ROAD_STATION_ID, NAME)
        SELECT :sensorConstantLotjuId, tms_rs.rs_id, :sensorConstantName
        FROM TMS_RS
        ON CONFLICT (LOTJU_ID)
        DO UPDATE SET
          name = :sensorConstantName,
          road_station_id = excluded.ROAD_STATION_ID,
          obsolete_date = null
        WHERE TMS_SENSOR_CONSTANT.name <> :sensorConstantName
           OR TMS_SENSOR_CONSTANT.road_station_id <> excluded.ROAD_STATION_ID
           OR TMS_SENSOR_CONSTANT.obsolete_date is not null""";

    private final static String UPSERT_CONSTANT_VALUES_STATEMENT = """
        WITH sc AS (
          SELECT lotju_id
          FROM TMS_SENSOR_CONSTANT
          WHERE LOTJU_ID = :sensorConstantLotjuId
        )
        INSERT INTO TMS_SENSOR_CONSTANT_VALUE (LOTJU_ID, SENSOR_CONSTANT_LOTJU_ID, VALUE, VALID_FROM, VALID_TO)
        SELECT :sensorConstantValueLotjuId, sc.LOTJU_ID, :sensorConstantValue, :sensorConstantValidFrom, :sensorConstantValidTo
        FROM sc
        ON CONFLICT (LOTJU_ID)
        DO UPDATE set
            sensor_constant_lotju_id = EXCLUDED.sensor_constant_lotju_id,
            value = EXCLUDED.value,
            valid_from = EXCLUDED.valid_from,
            valid_to = EXCLUDED.valid_to,
            obsolete_date = null
        WHERE TMS_SENSOR_CONSTANT_VALUE.sensor_constant_lotju_id <> EXCLUDED.sensor_constant_lotju_id
           OR TMS_SENSOR_CONSTANT_VALUE.value <> EXCLUDED.value
           OR TMS_SENSOR_CONSTANT_VALUE.valid_from <> EXCLUDED.valid_from
           OR TMS_SENSOR_CONSTANT_VALUE.valid_to <> EXCLUDED.valid_to
           OR TMS_SENSOR_CONSTANT_VALUE.obsolete_date is not null""";

    @Autowired
    public TmsSensorConstantDao(final NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public int obsoleteSensorConstantsExcludingIds(final Collection<Long> excludeLotjuIds) {
        final Map<String, Collection<Long>> paramMap = Collections.singletonMap("ids", excludeLotjuIds);
        return namedParameterJdbcTemplate.update(
            "UPDATE TMS_SENSOR_CONSTANT\n" +
            "SET OBSOLETE_DATE = now()\n" +
            "WHERE lotju_id not in (:ids)\n" +
            "  AND obsolete_date is null",
            paramMap);
    }

    public int obsoleteSensorConstantWithLotjuId(final Long lotjuId) {
        final Map<String, Long> paramMap = Collections.singletonMap("id", lotjuId);
        return namedParameterJdbcTemplate.update("""
                UPDATE TMS_SENSOR_CONSTANT
                SET OBSOLETE_DATE = now()
                WHERE lotju_id = (:id)
                AND obsolete_date is null""",
            paramMap);
    }

    public int obsoleteSensorConstantsWithRoadStationLotjuId(final Long roadStationLotjuId) {
        final Map<String, Long> paramMap = Collections.singletonMap("roadStationLotjuId", roadStationLotjuId);
        return namedParameterJdbcTemplate.update("""
                UPDATE TMS_SENSOR_CONSTANT
                SET OBSOLETE_DATE = now()
                WHERE tms_sensor_constant.road_station_id in (
                   select rs.id   from road_station rs
                   where rs.lotju_id = (:roadStationLotjuId))
                AND obsolete_date is null""",
            paramMap);
    }

    public int updateSensorConstants(final List<LamAnturiVakioVO> allLamAnturiVakios) {

        final ArrayList<Map<String, Object>> batchData = new ArrayList<>();
        allLamAnturiVakios.forEach(lamAnturiVakio -> {
            final HashMap<String, Object> params = new HashMap<>();
            params.put("sensorConstantLotjuId", lamAnturiVakio.getId());
            params.put("sensorConstantName", lamAnturiVakio.getNimi());
            params.put("sensorConstantRoadStationLotjuId", lamAnturiVakio.getAsemaId());
            batchData.add(params);
        });

        final int[] upserts = namedParameterJdbcTemplate.batchUpdate(
            UPSERT_CONSTANTS_STATEMENT,
            batchData.toArray(new Map[0]));

        return countUpserts(upserts);
    }

    public int updateSensorConstantValues(final List<LamAnturiVakioArvoVO> allLamAnturiVakioArvos) {

        final ArrayList<Map<String, Object>> batchData = new ArrayList<>();
        allLamAnturiVakioArvos.forEach(v -> {
            final HashMap<String, Object> params = new HashMap<>();
            params.put("sensorConstantValueLotjuId", v.getId());
            params.put("sensorConstantLotjuId", v.getAnturiVakioId());
            params.put("sensorConstantValue", v.getArvo());
            params.put("sensorConstantValidFrom", v.getVoimassaAlku());
            params.put("sensorConstantValidTo", v.getVoimassaLoppu());
            batchData.add(params);
        });

        final int[] upserts = namedParameterJdbcTemplate.batchUpdate(
                UPSERT_CONSTANT_VALUES_STATEMENT,
                batchData.toArray(new Map[0]));

        return countUpserts(upserts);
    }

    private int countUpserts(final int[] upserts) {
        int count = 0;
        for (final int value : upserts) {
            if (value >= 0) {
                count += value;
            } else if (value == -2) {
                count += 1;
            } else {
                log.error("Upsert return value {}", value);
            }
        }
        return count;
    }

    public int updateSensorSensorConstantValuesToObsoleteExcludingIds(final Collection<Long> excludeLotjuIds) {
        final Map<String, Collection<Long>> paramMap = Collections.singletonMap("ids", excludeLotjuIds);
        return namedParameterJdbcTemplate.update("""
                UPDATE TMS_SENSOR_CONSTANT_VALUE
                SET OBSOLETE_DATE = now()
                WHERE lotju_id not in (:ids)
                AND obsolete_date is null""",
            paramMap);
    }

    public int updateSensorConstantValueToObsoleteWithSensorConstantValueLotjuId(final long sensorConstantValueLotjuId) {
        final Map<String, Long> paramMap = Collections.singletonMap("sensorConstantValueLotjuId", sensorConstantValueLotjuId);
        return namedParameterJdbcTemplate.update("""
                UPDATE TMS_SENSOR_CONSTANT_VALUE
                SET OBSOLETE_DATE = now()
                WHERE lotju_id = :sensorConstantValueLotjuId
                AND obsolete_date is null""",
            paramMap);
    }
}