package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.model.Link;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE LINK SET name = :name, length = :length, direction = :direction, " +
                   "start_road_address_distance = :startRoadAddressDistance, end_road_address_distance = :endRoadAddressDistance, " +
                   "start_road_section_id = (select id from ROAD_SECTION where road_id = (select id from ROAD where natural_id = :startRoadNumber) and natural_id = :startRoadSectionNumber), " +
                   "end_road_section_id = (select id from ROAD_SECTION where road_id = (select id from ROAD where natural_id = :endRoadNumber) and natural_id = :endRoadSectionNumber), " +
                   "road_district_id = (select road_district_id from ROAD_SECTION where road_id = (select id from ROAD where natural_id = :startRoadNumber) and natural_id = :startRoadSectionNumber), " +
                   "special = :special, obsolete = 0, obsolete_date = null " +
                   "WHERE natural_id = :linkNaturalId",
           nativeQuery = true)
    void updateLink(@Param("startRoadNumber") final int startRoadNumber,
                    @Param("startRoadSectionNumber") final int startRoadSectionNumber,
                    @Param("endRoadNumber") final int endRoadNumber,
                    @Param("endRoadSectionNumber") final int endRoadSectionNumber,
                    @Param("name") final String name,
                    @Param("length") final long length,
                    @Param("direction") final int direction,
                    @Param("startRoadAddressDistance") final int startRoadAddressDistance,
                    @Param("endRoadAddressDistance") final int endRoadAddressDistance,
                    @Param("special") final int special,
                    @Param("linkNaturalId") final long linkNaturalId);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE LINK SET obsolete = 1, obsolete_date = sysdate " +
                   "WHERE obsolete = 0 AND obsolete_date IS NULL",
           nativeQuery = true)
    void makeNonObsoleteLinksObsolete();

    List<Link> findByOrderByNaturalId();
}
