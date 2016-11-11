package fi.livi.digitraffic.tie.metadata.dao;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fi.livi.digitraffic.tie.data.dto.SensorValueDto;

public interface RoadStationSensorValueDtoRepository extends JpaRepository<SensorValueDto, Long> {

    @Query(value =
            "select rs.natural_id road_station_natural_id\n" +
            "     , rs.id road_station_id\n" +
            "     , s.natural_id sensor_natural_id\n" +
            "     , s.id sensor_id\n" +
            "     , sv.id sensor_value_id\n" +
            "     , sv.value sensor_value\n" +
            "     , sv.measured\n" +
            "     , s.name sensor_name_old\n" +
            "     , s.name_fi sensor_name_fi\n" +
            "     , s.short_name_fi sensor_short_name_fi\n" +
            "     , s.unit sensor_unit\n" +
            "     , svd.description_fi as sensor_value_description_fi\n" +
            "     , svd.description_en as sensor_value_description_en\n" +
            "     , max(sv.measured) over(partition by sv.road_station_id) station_latest_measured\n" +
            "     , sv.updated\n" +
            "from road_station rs\n" +
            "inner join sensor_value sv on sv.road_station_id = rs.id\n" +
            "inner join road_station_sensor s on sv.road_station_sensor_id = s.id\n" +
            "left outer join sensor_value_description svd on svd.sensor_id = sv.road_station_sensor_id\n" +
            "                                            and svd.sensor_value = sv.value\n" +
            "where rs.type = :stationTypeId\n" +
            "  and rs.obsolete = 0\n" +
            "  and s.obsolete = 0\n" +
            "  and sv.measured > (\n" +
            "    select max(sensv.measured) - NUMTODSINTERVAL(:timeLimitInMinutes, 'MINUTE')\n" +
            "    from sensor_value sensv\n" +
            "    where sensv.road_station_id = sv.road_station_id\n" +
            "  )\n" +
            "  and exists (\n" +
            "     select null\n" +
            "     from allowed_road_station_sensor allowed\n" +
            "     where allowed.natural_id = s.natural_id\n" +
            "       and allowed.road_station_type = s.road_station_type\n" +
            "  )\n" +
            "order by rs.natural_id, s.natural_id",
           nativeQuery = true)
    // sensor typeid 2 = rws
    List<SensorValueDto> findAllPublicNonObsoleteRoadStationSensorValues(
            @Param("stationTypeId")
            final int stationTypeId,
            @Param("timeLimitInMinutes")
            final int timeLimitInMinutes);

        @Query(value =
            "select rs.natural_id road_station_natural_id\n" +
            "     , rs.id road_station_id\n" +
            "     , s.natural_id sensor_natural_id\n" +
            "     , s.id sensor_id\n" +
            "     , sv.id sensor_value_id\n" +
            "     , sv.value sensor_value\n" +
            "     , sv.measured\n" +
            "     , s.name sensor_name_old\n" +
            "     , s.name_fi sensor_name_fi\n" +
            "     , s.short_name_fi sensor_short_name_fi\n" +
            "     , s.unit sensor_unit\n" +
            "     , svd.description_fi as sensor_value_description_fi\n" +
            "     , svd.description_en as sensor_value_description_en\n" +
            "     , max(sv.measured) over(partition by sv.road_station_id) station_latest_measured\n" +
            "     , sv.updated\n" +
            "from road_station rs\n" +
            "inner join sensor_value sv on sv.road_station_id = rs.id\n" +
            "inner join road_station_sensor s on sv.road_station_sensor_id = s.id\n" +
            "left outer join sensor_value_description svd on svd.sensor_id = sv.road_station_sensor_id\n" +
            "                                            and svd.sensor_value = sv.value\n" +
            "where rs.type = :stationTypeId\n" +
            "  and rs.natural_id = :stationNaturalId\n" +
            "  and rs.obsolete = 0\n" +
            "  and s.obsolete = 0\n" +
            "  and sv.measured > (\n" +
            "    select max(sensv.measured) - NUMTODSINTERVAL(:timeLimitInMinutes, 'MINUTE')\n" +
            "    from sensor_value sensv\n" +
            "    where sensv.road_station_id = sv.road_station_id\n" +
            "  )\n" +
            "  and exists (\n" +
            "     select null\n" +
            "     from allowed_road_station_sensor allowed\n" +
            "     where allowed.natural_id = s.natural_id\n" +
            "       and allowed.road_station_type = s.road_station_type\n" +
            "  )\n" +
            "order by rs.natural_id, s.natural_id",
           nativeQuery = true)
    List<SensorValueDto> findAllPublicNonObsoleteRoadStationSensorValues(
            @Param("stationNaturalId")
            final long stationNaturalId,
            @Param("stationTypeId")
            final int stationTypeId,
            @Param("timeLimitInMinutes")
            final int timeLimitInMinutes);


    @Query(value =
                   "select rs.natural_id road_station_natural_id\n" +
                   "     , rs.id road_station_id\n" +
                   "     , s.natural_id sensor_natural_id\n" +
                   "     , s.id sensor_id\n" +
                   "     , sv.id sensor_value_id\n" +
                   "     , sv.value sensor_value\n" +
                   "     , sv.measured\n" +
                   "     , s.name sensor_name_old\n" +
                   "     , s.name_fi sensor_name_fi\n" +
                   "     , s.short_name_fi sensor_short_name_fi\n" +
                   "     , s.unit sensor_unit\n" +
                   "     , svd.description_fi as sensor_value_description_fi\n" +
                   "     , svd.description_en as sensor_value_description_en\n" +
                   "     , max(sv.measured) over(partition by sv.road_station_id) station_latest_measured\n" +
                   "     , sv.updated\n" +
                   "from road_station rs\n" +
                   "inner join sensor_value sv on sv.road_station_id = rs.id\n" +
                   "inner join road_station_sensor s on sv.road_station_sensor_id = s.id\n" +
                   "left outer join sensor_value_description svd on svd.sensor_id = sv.road_station_sensor_id\n" +
                   "                                            and svd.sensor_value = sv.value\n" +
                   "where rs.type = :stationTypeId\n" +
                   "  and rs.obsolete = 0\n" +
                   "  and s.obsolete = 0\n" +
                   "  and sv.updated > :afterDate\n" +
                   "  and exists (\n" +
                   "     select null\n" +
                   "     from allowed_road_station_sensor allowed\n" +
                   "     where allowed.natural_id = s.natural_id\n" +
                   "       and allowed.road_station_type = s.road_station_type\n" +
                   "  )\n" +
                   "order by sv.updated",
                   nativeQuery = true)
    List<SensorValueDto> findAllPublicNonObsoleteRoadStationSensorValuesUpdatedAfter (
            @Param("stationTypeId")
            final int stationTypeId,
            @Param("afterDate")
            final Date afterDate);

    @Query(value =
           "select max(sv.measured) as updated\n" +
           "from road_station rs\n" +
           "inner join sensor_value sv on sv.road_station_id = rs.id\n" +
           "inner join road_station_sensor s on sv.road_station_sensor_id = s.id\n" +
           "where rs.type = :stationTypeId\n" +
           "  and rs.obsolete = 0\n" +
           "  and s.obsolete = 0\n" +
           "  and sv.measured > (\n" +
           "    select max(sensv.measured) - NUMTODSINTERVAL(:timeLimitInMinutes, 'MINUTE')\n" +
           "    from sensor_value sensv\n" +
           "    where sensv.road_station_id = sv.road_station_id\n" +
           "  )\n" +
           "  and exists (\n" +
           "     select null\n" +
           "     from allowed_road_station_sensor allowed\n" +
           "     where allowed.natural_id = s.natural_id\n" +
           "       and allowed.road_station_type = s.road_station_type\n" +
           "  )",
           nativeQuery = true)
    LocalDateTime getLatestMeasurementTime(
            @Param("stationTypeId")
            final int stationTypeId,
            @Param("timeLimitInMinutes")
            final int timeLimitInMinutes);
}
