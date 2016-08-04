package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.RoadAddress;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;

@Repository
public interface RoadAddressRepository extends JpaRepository<RoadAddress, Long>{

    @Query(value =
            "SELECT RA.*\n" +
            "FROM ROAD_ADDRESS RA\n" +
            "WHERE RA.ROAD_STATION_TYPE = :roadStationType",
            nativeQuery = true)
    List<RoadAddress> findByRoadStationType(@Param("roadStationType")
                                            RoadStationType roadStationType);
}