package fi.livi.digitraffic.tie.dao;

import fi.livi.digitraffic.tie.model.RoadDistrict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoadDistrictRepository extends JpaRepository<RoadDistrict, Long> {

    RoadDistrict findByNaturalId(final int roadDistrictNaturalId);

    @Query(value =
            "select RD.*\n" +
            "    from ROAD_DISTRICT RD\n" +
            "    inner join ROAD_SECTION RS on RS.ROAD_DISTRICT_ID = RD.ID\n" +
            "    inner join ROAD R on R.ID = RS.ROAD_ID\n" +
            "    where RS.NATURAL_ID = :roadSectionNaturalId\n" +
            "    and R.NATURAL_ID = :roadNaturalId",
            nativeQuery = true)
    RoadDistrict findByRoadSectionAndRoadNaturalId(@Param("roadSectionNaturalId") final int roadSectionNaturalId,
                                                   @Param("roadNaturalId") final int roadNaturalId);
}
