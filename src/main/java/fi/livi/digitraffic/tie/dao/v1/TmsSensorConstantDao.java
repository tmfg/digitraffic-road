package fi.livi.digitraffic.tie.dao.v1;

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

    private final static String UPSERT_CONSTANTS_STATEMENT =
        "WITH TMS_RS AS (\n" +
        "    SELECT rs.id as rs_id, rs.lotju_id rs_lotju_id\n" +
        "    FROM road_station rs\n" +
        "    WHERE rs.road_station_type = 'TMS_STATION'\n" +
        "      AND rs.lotju_id = :sensorConstantRoadStationLotjuId\n" +
        ")\n" +
        "INSERT INTO TMS_SENSOR_CONSTANT (LOTJU_ID, ROAD_STATION_ID, NAME)\n" +
        "SELECT :sensorConstantLotjuId, tms_rs.rs_id, :sensorConstantName\n" +
        "FROM TMS_RS\n" +
        "ON CONFLICT (LOTJU_ID)\n" +
        "DO UPDATE SET\n" +
        "  name = :sensorConstantName,\n" +
        "  road_station_id = excluded.ROAD_STATION_ID,\n" +
        "  obsolete_date = null\n" +
        "WHERE TMS_SENSOR_CONSTANT.name <> :sensorConstantName\n" +
        "   OR TMS_SENSOR_CONSTANT.road_station_id <> excluded.ROAD_STATION_ID\n" +
        "   OR TMS_SENSOR_CONSTANT.obsolete_date is not null";

    private final static String UPSERT_CONSTANT_VALUES_STATEMENT =
        "WITH sc AS (\n" +
        "  SELECT lotju_id\n" +
        "  FROM TMS_SENSOR_CONSTANT\n" +
        "  WHERE LOTJU_ID = :sensorConstantLotjuId\n" +
        ")\n" +
        "INSERT INTO TMS_SENSOR_CONSTANT_VALUE (LOTJU_ID, SENSOR_CONSTANT_LOTJU_ID, VALUE, VALID_FROM, VALID_TO)\n" +
        "SELECT :sensorConstantValueLotjuId, sc.LOTJU_ID, :sensorConstantValue, :sensorConstantValidFrom, :sensorConstantValidTo\n" +
        "FROM sc\n" +
        "ON CONFLICT (LOTJU_ID)\n" +
        "DO UPDATE set\n" +
        "    sensor_constant_lotju_id = EXCLUDED.sensor_constant_lotju_id,\n" +
        "    value = EXCLUDED.value,\n" +
        "    valid_from = EXCLUDED.valid_from,\n" +
        "    valid_to = EXCLUDED.valid_to,\n" +
        "    obsolete_date = null\n" +
        "WHERE TMS_SENSOR_CONSTANT_VALUE.sensor_constant_lotju_id <> EXCLUDED.sensor_constant_lotju_id\n" +
        "   OR TMS_SENSOR_CONSTANT_VALUE.value <> EXCLUDED.value\n" +
        "   OR TMS_SENSOR_CONSTANT_VALUE.valid_from <> EXCLUDED.valid_from\n" +
        "   OR TMS_SENSOR_CONSTANT_VALUE.valid_to <> EXCLUDED.valid_to\n" +
        "   OR TMS_SENSOR_CONSTANT_VALUE.obsolete_date is not null";

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

    public int obsoleteSensorConstant(final Long lotjuId) {
        final Map<String, Long> paramMap = Collections.singletonMap("id", lotjuId);
        return namedParameterJdbcTemplate.update(
            "UPDATE TMS_SENSOR_CONSTANT\n" +
                "SET OBSOLETE_DATE = now()\n" +
                "WHERE lotju_id = (:id)\n" +
                "  AND obsolete_date is null",
            paramMap);
    }

    public int updateSensorConstants(List<LamAnturiVakioVO> allLamAnturiVakios) {

        final ArrayList<Map<String, Object>> batchData = new ArrayList<>();
        allLamAnturiVakios.forEach(lamAnturiVakio -> {
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("sensorConstantLotjuId", lamAnturiVakio.getId());
            params.put("sensorConstantName", lamAnturiVakio.getNimi());
            params.put("sensorConstantRoadStationLotjuId", lamAnturiVakio.getAsemaId());
            batchData.add(params);
        });

        int[] upserts = namedParameterJdbcTemplate.batchUpdate(
            UPSERT_CONSTANTS_STATEMENT,
            batchData.toArray(new Map[0]));

        return countUpserts(upserts);
    }

    public int updateSensorConstantValues(final List<LamAnturiVakioArvoVO> allLamAnturiVakioArvos) {


        final ArrayList<Map<String, Object>> batchData = new ArrayList<>();
        allLamAnturiVakioArvos.forEach(v -> {
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("sensorConstantValueLotjuId", v.getId());
            params.put("sensorConstantLotjuId", v.getAnturiVakioId());
            params.put("sensorConstantValue", v.getArvo());
            params.put("sensorConstantValidFrom", v.getVoimassaAlku());
            params.put("sensorConstantValidTo", v.getVoimassaLoppu());
            batchData.add(params);
        });

        int[] upserts = namedParameterJdbcTemplate.batchUpdate(
                UPSERT_CONSTANT_VALUES_STATEMENT,
                batchData.toArray(new Map[0]));

        return countUpserts(upserts);
    }

    private int countUpserts(final int[] upserts) {
        int count = 0;
        for (int value : upserts) {
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

    public int obsoleteSensorConstantValuesExcludingIds(Collection<Long> excludeLotjuIds) {
        final Map<String, Collection<Long>> paramMap = Collections.singletonMap("ids", excludeLotjuIds);
        return namedParameterJdbcTemplate.update(
                "UPDATE TMS_SENSOR_CONSTANT_VALUE\n" +
                "SET OBSOLETE_DATE = now()\n" +
                "WHERE lotju_id not in (:ids)\n" +
                "AND obsolete_date is null",
            paramMap);
    }

    public int obsoleteSensorConstantValue(final long lotjuId) {
        final Map<String, Long> paramMap = Collections.singletonMap("id", lotjuId);
        return namedParameterJdbcTemplate.update(
            "UPDATE TMS_SENSOR_CONSTANT_VALUE\n" +
                "SET OBSOLETE_DATE = now()\n" +
                "WHERE lotju_id = (:id)\n" +
                "AND obsolete_date is null",
            paramMap);
    }
}