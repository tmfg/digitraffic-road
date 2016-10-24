package fi.livi.digitraffic.tie.metadata.dao.location;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.location.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {
    @Query(value = "select location_code, subtype_code, road_junction, road_name, first_name, second_name, area_ref, linear_ref, "
            + "neg_offset, pos_offset, urban, wgs84_lat, wgs84_long, neg_direction, pos_direction\n"
            + "from location", nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    Object[][] findAllLocations();

    @Query(value = "select location_code, subtype_code, road_junction, road_name, first_name, second_name, area_ref, linear_ref, "
            + "neg_offset, pos_offset, urban, wgs84_lat, wgs84_long, neg_direction, pos_direction\n"
            + "from location\n"
            + "where location_code=:locationCode", nativeQuery = true)
    Object[][] findLocation(@Param("locationCode") final int locationCode);
}
