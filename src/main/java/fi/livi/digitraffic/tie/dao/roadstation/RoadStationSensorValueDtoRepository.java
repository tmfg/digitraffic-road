package fi.livi.digitraffic.tie.dao.roadstation;

import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import fi.livi.digitraffic.tie.dto.v1.SensorValueDto;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import jakarta.persistence.QueryHint;

public interface RoadStationSensorValueDtoRepository extends JpaRepository<SensorValueDto, Long> {
    @QueryHints(@QueryHint(name=HINT_FETCH_SIZE, value="3000"))
    @Query(value = """
        select rs.natural_id road_station_natural_id
             , s.natural_id sensor_natural_id
             , sv.id sensor_value_id
             , sv.value sensor_value
             , sv.time_window_start
             , sv.time_window_end
             , sv.measured as measured_time
             , s.name sensor_name_old
             , s.name_fi sensor_name_fi
             , s.short_name_fi sensor_short_name_fi
             , s.unit sensor_unit
             , svd.description_fi as sensor_value_description_fi
             , svd.description_en as sensor_value_description_en
             , max(sv.measured) over(partition by sv.road_station_id) station_latest_measured_time
             , sv.updated as updated_time
        from road_station rs
        inner join sensor_value sv on sv.road_station_id = rs.id
        inner join road_station_sensor s on sv.road_station_sensor_id = s.id
        left outer join sensor_value_description svd on svd.sensor_id = sv.road_station_sensor_id
                                                   and svd.sensor_value = sv.value
        where rs.road_station_type = :#{#roadStationType.name()}
         and rs.publishable = true
         and s.publishable = true
         and sv.measured > (now() -(:timeLimitInMinutes * interval '1 MINUTE'))
         and exists (
            select null
            from allowed_road_station_sensor allowed
            where allowed.natural_id = s.natural_id
              and allowed.road_station_type = s.road_station_type
         )""", nativeQuery = true)
    List<SensorValueDto> findAllPublicPublishableRoadStationSensorValues(
            final RoadStationType roadStationType,
            final int timeLimitInMinutes);

    @QueryHints(@QueryHint(name=HINT_FETCH_SIZE, value="3000"))
    @Query(value = """
        select rs.natural_id road_station_natural_id
             , s.natural_id sensor_natural_id
             , sv.id sensor_value_id
             , sv.value sensor_value
             , sv.time_window_start
             , sv.time_window_end
             , sv.measured as measured_time
             , s.name sensor_name_old
             , s.name_fi sensor_name_fi
             , s.short_name_fi sensor_short_name_fi
             , s.unit sensor_unit
             , svd.description_fi as sensor_value_description_fi
             , svd.description_en as sensor_value_description_en
             , max(sv.measured) over(partition by sv.road_station_id) station_latest_measured_time
             , sv.updated as updated_time
        from road_station rs
        inner join sensor_value sv on sv.road_station_id = rs.id
        inner join road_station_sensor s on sv.road_station_sensor_id = s.id
        left outer join sensor_value_description svd on svd.sensor_id = sv.road_station_sensor_id
                                                    and svd.sensor_value = sv.value
        where rs.road_station_type = :#{#roadStationType.name()}
          and rs.natural_id = :stationNaturalId
          and rs.publishable = true
          and s.publishable = true
          and sv.measured > (now() -(:timeLimitInMinutes * interval '1 MINUTE'))
          and exists (
            select null
            from allowed_road_station_sensor allowed
            where allowed.natural_id = s.natural_id
              and allowed.road_station_type = s.road_station_type
          )
        order by rs.natural_id, s.natural_id""", nativeQuery = true)

    List<SensorValueDto> findAllPublicPublishableRoadStationSensorValues(
            final long stationNaturalId,
            final RoadStationType roadStationType,
            final int timeLimitInMinutes);

    @QueryHints(@QueryHint(name=HINT_FETCH_SIZE, value="3000"))
    @Query(value = """
        select rs.natural_id road_station_natural_id
             , s.natural_id sensor_natural_id
             , sv.id sensor_value_id
             , sv.value sensor_value
             , sv.time_window_start
             , sv.time_window_end
             , sv.measured as measured_time
             , s.name sensor_name_old
             , s.name_fi sensor_name_fi
             , s.short_name_fi sensor_short_name_fi
             , s.unit sensor_unit
             , svd.description_fi as sensor_value_description_fi
             , svd.description_en as sensor_value_description_en
             , max(sv.measured) over(partition by sv.road_station_id) station_latest_measured_time     , sv.updated as updated_time
        from road_station rs
        inner join sensor_value sv on sv.road_station_id = rs.id
        inner join road_station_sensor s on sv.road_station_sensor_id = s.id
        left outer join sensor_value_description svd on svd.sensor_id = sv.road_station_sensor_id
                                                    and svd.sensor_value = sv.value
        where rs.road_station_type = :#{#roadStationType.name()}
          and rs.publishable = true
          and s.publishable = true
          and sv.updated > :afterDate
          and exists (
             select null
             from allowed_road_station_sensor allowed
             where allowed.natural_id = s.natural_id
               and allowed.road_station_type = s.road_station_type
          )
        order by sv.updated""", nativeQuery = true)
    List<SensorValueDto> findAllPublicPublishableRoadStationSensorValuesUpdatedAfter(
            final RoadStationType roadStationType,
            final Instant afterDate);
}
