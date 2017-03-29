package fi.livi.digitraffic.tie.metadata.dao;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LinkDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public LinkDao(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createOrUpdateLink(final int startRoadNumber, final int startRoadSectionNumber, final int endRoadNumber,
                                   final int endRoadSectionNumber, final String name, final String nameSv, final String nameEn,
                                   final long length, final int direction, final int startRoadAddressDistance,
                                   final int endRoadAddressDistance, final int special, final long linkNaturalId) {

        final HashMap<String, Object> args = new HashMap<>();
        args.put("startRoadNumber", startRoadNumber);
        args.put("startRoadSectionNumber", startRoadSectionNumber);
        args.put("endRoadNumber", endRoadNumber);
        args.put("endRoadSectionNumber", endRoadSectionNumber);
        args.put("name", name);
        args.put("nameSv", nameSv);
        args.put("nameEn", nameEn);
        args.put("length", length);
        args.put("direction", direction);
        args.put("startRoadAddressDistance", startRoadAddressDistance);
        args.put("endRoadAddressDistance", endRoadAddressDistance);
        args.put("special", special);
        args.put("linkNaturalId", linkNaturalId);

        jdbcTemplate.update(
            "MERGE INTO LINK s " +
            "USING (SELECT :linkNaturalId natural_id FROM dual) src " +
            "ON (s.natural_id = src.natural_id) " +
            "WHEN MATCHED THEN " +
            "UPDATE SET name = :name, name_sv = :nameSv, name_en = :nameEn, length = :length, direction = :direction, " +
                "start_road_address_distance = :startRoadAddressDistance, end_road_address_distance = :endRoadAddressDistance, " +
                "start_road_section_id = (select id from ROAD_SECTION where road_id = (select id from ROAD where natural_id = :startRoadNumber) and natural_id = :startRoadSectionNumber), " +
                "end_road_section_id = (select id from ROAD_SECTION where road_id = (select id from ROAD where natural_id = :endRoadNumber) and natural_id = :endRoadSectionNumber), " +
                "road_district_id = (select road_district_id from ROAD_SECTION where road_id = (select id from ROAD where natural_id = :startRoadNumber) and natural_id = :startRoadSectionNumber), " +
                "special = :special, obsolete = 0, obsolete_date = null " +
            "WHEN NOT MATCHED THEN " +
            "INSERT (id, natural_id, name, name_sv, name_en, length, direction, start_road_address_distance, end_road_address_distance," +
                    "start_road_section_id, end_road_section_id, road_district_id, " +
                    "special, obsolete, obsolete_date, summer_free_flow_speed, winter_free_flow_speed) " +
            "VALUES (SEQ_LINK.nextval, :linkNaturalId, :name, :nameSv, :nameEn, :length, :direction, " +
                    ":startRoadAddressDistance, :endRoadAddressDistance, " +
                    "(select id from ROAD_SECTION where road_id = (select id from ROAD where natural_id = :startRoadNumber) and natural_id = :startRoadSectionNumber)," +
                    "(select id from ROAD_SECTION where road_id = (select id from ROAD where natural_id = :endRoadNumber) and natural_id = :endRoadSectionNumber)," +
                    "(select road_district_id from ROAD_SECTION where road_id = (select id from ROAD where natural_id = :startRoadNumber) and natural_id = :startRoadSectionNumber)," +
                    ":special, 0, null, 0, 0)",
            args);
    }

    public void createOrUpdateLinkSites(final long linkNaturalId, final List<Integer> siteNaturalIds) {

        final HashMap<String, Object> args = new HashMap<>();
        args.put("linkNaturalId", linkNaturalId);
        jdbcTemplate.update("DELETE FROM LINK_SITE WHERE LINK_ID = (SELECT id FROM LINK WHERE natural_id = :linkNaturalId)", args);

        int orderNumber = 1;
        for (final Integer siteNaturalId : siteNaturalIds) {
            args.clear();
            args.put("linkNaturalId", linkNaturalId);
            args.put("siteId", siteNaturalId);
            args.put("orderNumber", orderNumber);

            jdbcTemplate.update("INSERT INTO LINK_SITE (LINK_ID, SITE_ID, ORDER_NUMBER) " +
                                "VALUES ((SELECT id FROM LINK WHERE natural_id = :linkNaturalId), :siteId, :orderNumber)", args);
            orderNumber++;
        }
    }

    public void createOrUpdateLinkDirection(final long linkNaturalId, final int directionNaturalId) {

        final HashMap<String, Object> args = new HashMap<>();
        args.put("linkNaturalId", linkNaturalId);
        jdbcTemplate.update("DELETE FROM LINK_DIRECTION WHERE LINK_ID = (SELECT id FROM LINK WHERE natural_id = :linkNaturalId)", args);

        args.put("directionNaturalId", directionNaturalId);
        jdbcTemplate.update("INSERT INTO LINK_DIRECTION (LINK_ID, DIRECTION_ID) " +
                            "VALUES ((SELECT id FROM LINK WHERE natural_id = :linkNaturalId), :directionNaturalId)", args);
    }
}
