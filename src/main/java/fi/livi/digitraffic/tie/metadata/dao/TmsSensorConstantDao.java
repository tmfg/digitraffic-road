package fi.livi.digitraffic.tie.metadata.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamAnturiVakioArvoVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamAnturiVakioVO;

@Repository
public class TmsSensorConstantDao {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final static String UPSERT_CONSTANTS_STATEMENT =
        "WITH TMS_RS AS (\n" +
        "    select rs.id as rs_id, rs.lotju_id rs_lotju_id\n" +
        "    from road_station rs\n" +
        "    where rs.road_station_type = 'TMS_STATION'\n" +
        ")\n" +
        "insert into TMS_SENSOR_CONSTANT (ID, LOTJU_ID, ROAD_STATION_ID, NAME)\n" +
        "select nextval('SEQ_TMS_SENSOR_CONSTANT'), :sensorConstantLotjuId, tms_rs.rs_id, :sensorConstantName\n" +
        "from TMS_RS\n" +
        "where tms_rs.rs_lotju_id = :sensorConstantRoadStationLotjuId\n" +
        "on conflict (LOTJU_ID)\n" +
        "do update set name = :sensorConstantName,  obsolete_date = null";

    private final static String INSERT_CONSTANT_VALUES_STATEMENT =
        "insert into TMS_SENSOR_CONSTANT_VALUE(ID, SENSOR_CONSTANT_ID, VALUE, VALID_FROM, VALID_TO)\n" +
        "select nextval('SEQ_TMS_SENSOR_CONSTANT_VALUE'), tsc.id, :sensorConstantValue, :sensorConstantValidFrom, :sensorConstantValidTo\n" +
        "from TMS_SENSOR_CONSTANT tsc\n" +
        "where tsc.lotju_id = :sensorConstantLotjuId";

    // Gets free flow speeds for winter at 1.1. and for summer at 1.7.
    private final String UPDATE_FREE_FLOW_SPEEDS =
        "WITH sv AS (\n" +
        "    select sc.ROAD_STATION_ID, rs.lotju_id as rs_lotju_id, sc.NAME, scv.VALID_FROM, scv.VALID_TO, scv.VALUE\n" +
        "    from TMS_SENSOR_CONSTANT sc\n" +
        "    inner join TMS_SENSOR_CONSTANT_VALUE scv on scv.SENSOR_CONSTANT_ID = sc.ID\n" +
        "    inner join road_station rs on rs.id = sc.ROAD_STATION_ID\n" +
        "    where sc.NAME like 'VVAPAAS%'\n" +
        "      and sc.OBSOLETE_DATE is null)\n" +
        ", winter_free_flow_speed_1 as (\n" +
        "    SELECT sv.ROAD_STATION_ID, sv.NAME, sv.VALID_FROM, sv.VALID_TO, sv.VALUE\n" +
        "    from sv\n" +
        "    where (sv.VALID_FROM = 101 or sv.VALID_TO = 101 OR (sv.VALID_FROM >= 101 and sv.VALID_TO > 101 and sv.VALID_FROM >= sv.VALID_TO))\n" +
        "      and sv.name = 'VVAPAAS1'\n" +
        "), winter_free_flow_speed_2 as (\n" +
        "    SELECT sv.ROAD_STATION_ID, sv.NAME, sv.VALID_FROM, sv.VALID_TO, sv.VALUE\n" +
        "    from sv\n" +
        "    where (sv.VALID_FROM = 101 or sv.VALID_TO = 101 OR (sv.VALID_FROM >= 101 and sv.VALID_TO > 101 and sv.VALID_FROM >= sv.VALID_TO))\n" +
        "      and sv.name = 'VVAPAAS2'\n" +
        "), summer_free_flow_speed_1 as (\n" +
        "    SELECT sv.ROAD_STATION_ID, sv.NAME, sv.VALID_FROM, sv.VALID_TO, sv.VALUE\n" +
        "    from sv\n" +
        "    where ((sv.VALID_FROM <= 701 and sv.VALID_TO >= 701)\n" +
        "        OR (sv.VALID_FROM >= 701 and sv.VALID_TO >= 701 and sv.VALID_FROM >= sv.VALID_TO)\n" +
        "        OR (sv.VALID_FROM <= 701 and sv.VALID_TO <= 701 and sv.VALID_FROM >= sv.VALID_TO))\n" +
        "      and sv.name = 'VVAPAAS1'\n" +
        "), summer_free_flow_speed_2 as (\n" +
        "    SELECT sv.ROAD_STATION_ID, sv.NAME, sv.VALID_FROM, sv.VALID_TO, sv.VALUE\n" +
        "    from sv\n" +
        "    where ((sv.VALID_FROM <= 701 and sv.VALID_TO >= 701)\n" +
        "        OR (sv.VALID_FROM >= 701 and sv.VALID_TO >= 701 and sv.VALID_FROM >= sv.VALID_TO)\n" +
        "        OR (sv.VALID_FROM <= 701 and sv.VALID_TO <= 701 and sv.VALID_FROM >= sv.VALID_TO))\n" +
        "      and sv.name = 'VVAPAAS2'\n" +
        "), data as (\n" +
        "    select distinct\n" +
        "           sv.ROAD_STATION_ID,\n" +
        "           w1.VALUE as winter_free_flow_speed_1,\n" +
        "           w2.VALUE as winter_free_flow_speed_2,\n" +
        "           s1.VALUE as summer_free_flow_speed_1,\n" +
        "           s2.VALUE as summer_free_flow_speed_2\n" +
        "    from sv\n" +
        "    left outer join winter_free_flow_speed_1 w1 on w1.ROAD_STATION_ID = sv.ROAD_STATION_ID\n" +
        "    left outer join winter_free_flow_speed_2 w2 on w2.ROAD_STATION_ID = sv.ROAD_STATION_ID\n" +
        "    left outer join summer_free_flow_speed_1 s1 on s1.ROAD_STATION_ID = sv.ROAD_STATION_ID\n" +
        "    left outer join summer_free_flow_speed_2 s2 on s2.ROAD_STATION_ID = sv.ROAD_STATION_ID\n" +
        ")\n" +
        "update lam_station\n" +
        "set summer_free_flow_speed_1 = data.summer_free_flow_speed_1,\n" +
        "    summer_free_flow_speed_2 = data.summer_free_flow_speed_2,\n" +
        "    winter_free_flow_speed_1 = data.winter_free_flow_speed_1,\n" +
        "    winter_free_flow_speed_2 = data.winter_free_flow_speed_2\n" +
        "from data\n" +
        "where lam_station.road_station_id = data.ROAD_STATION_ID";


    @Autowired
    public TmsSensorConstantDao(final NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public int obsoleteSensorConstants(Collection<Long> excludeLotjuIds) {
        final Map<String, Collection<Long>> paramMap = Collections.singletonMap("ids", excludeLotjuIds);
        return namedParameterJdbcTemplate.update(
            "UPDATE TMS_SENSOR_CONSTANT\n" +
            "SET OBSOLETE_DATE = now()\n" +
            "WHERE lotju_id in (:ids)\n" +
            "AND obsolete_date is null",
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

        int[] updateCount = namedParameterJdbcTemplate.batchUpdate(
            UPSERT_CONSTANTS_STATEMENT,
            batchData.toArray(new Map[0]));

        int countUpdates = 0;
        for (int i : updateCount) {
            countUpdates += i;
        }
        return countUpdates;
    }

    public int updateSensorConstantValues(final List<LamAnturiVakioArvoVO> allLamAnturiVakioArvos) {

        namedParameterJdbcTemplate.getJdbcTemplate().execute("DELETE FROM TMS_SENSOR_CONSTANT_VALUE");

        final ArrayList<Map<String, Object>> batchData = new ArrayList<>();
        allLamAnturiVakioArvos.forEach(v -> {
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("sensorConstantLotjuId", v.getAnturiVakioId());
            params.put("sensorConstantValue", v.getArvo());
            params.put("sensorConstantValidFrom", v.getVoimassaAlku());
            params.put("sensorConstantValidTo", v.getVoimassaLoppu());
            batchData.add(params);
        });

        int inserted[] =
            namedParameterJdbcTemplate.batchUpdate(
                INSERT_CONSTANT_VALUES_STATEMENT,
                batchData.toArray(new Map[0]));

        int countInserted = 0;
        for (int i : inserted) {
            if (i > 1) {
                System.out.println("What?");
            }
            countInserted += i;
        }
        return countInserted;
    }

    public int updateFreeFlowSpeedsOfTmsStations() {
        return namedParameterJdbcTemplate.getJdbcTemplate().update(UPDATE_FREE_FLOW_SPEEDS);
    }
}