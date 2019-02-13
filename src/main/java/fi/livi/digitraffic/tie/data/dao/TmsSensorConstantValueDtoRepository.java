package fi.livi.digitraffic.tie.data.dao;

import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import fi.livi.digitraffic.tie.data.dto.tms.TmsSensorConstantValueDto;

public interface TmsSensorConstantValueDtoRepository extends JpaRepository<TmsSensorConstantValueDto, Long> {

    @Query(value =
               "select scv.LOTJU_ID, sc.NAME, scv.VALUE, scv.VALID_FROM, scv.VALID_TO, rs.natural_id as road_Station_Id\n" +
               "from TMS_SENSOR_CONSTANT sc\n" +
               "inner join tms_sensor_constant_value scv on scv.SENSOR_CONSTANT_LOTJU_ID = sc.LOTJU_ID\n" +
               "inner join road_station rs on rs.id = sc.ROAD_STATION_ID\n" +
               "where exists(select null from ALLOVED_TMS_SENSOR_CONSTANTS a where a.NAME = sc.NAME)\n" +
               "  and sc.OBSOLETE_DATE is null\n" +
               "  and scv.OBSOLETE_DATE is null\n" +
               "  and rs.publishable = true\n" +
               "order by ROAD_STATION_ID, sc.NAME",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<TmsSensorConstantValueDto> findAllPublishableSensorConstantValues();
}
