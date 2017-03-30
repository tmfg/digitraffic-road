package fi.livi.digitraffic.tie.metadata.dao;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SiteDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public SiteDao(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createOrUpdateSite(final long siteNaturalId, final String nameFi, final String nameSv, final String nameEn,
                                   final int roadNumber, final int roadSectionNumber, final Long xCoordKkj3, final Long yCoordKkj3,
                                   final Double longitudeWgs84, final Double latitudeWgs84) {

        final HashMap<String, Object> args = new HashMap<>();
        args.put("naturalId", siteNaturalId);
        args.put("nameFi", nameFi);
        args.put("nameSv", nameSv);
        args.put("nameEn", nameEn);
        args.put("roadNumber", roadNumber);
        args.put("roadSectionNumber", roadSectionNumber);
        args.put("xCoord", xCoordKkj3);
        args.put("yCoord", yCoordKkj3);
        args.put("longitude", longitudeWgs84);
        args.put("latitude", latitudeWgs84);

        jdbcTemplate.update(
            "MERGE INTO SITE s " +
                "USING (SELECT :naturalId natural_id FROM DUAL) src " +
                "ON (s.natural_id = src.natural_id) " +
                "WHEN NOT MATCHED THEN " +
                "INSERT (natural_id, name_fi, name_sv, name_en, road_section_id, x_coord_kkj3, y_coord_kkj3, longitude_wgs84, latitude_wgs84, obsolete_date) " +
                "VALUES (:naturalId, :nameFi, :nameSv, :nameEn, " +
                "          (SELECT id FROM ROAD_SECTION WHERE natural_id = :roadSectionNumber and road_id = (SELECT id FROM ROAD WHERE natural_id = :roadNumber)), " +
                "          :xCoord, :yCoord, :longitude, :latitude, null) " +
                "WHEN MATCHED THEN " +
                "UPDATE SET s.name_fi = :nameFi, s.name_sv = :nameSv, s.name_en = :nameEn, " +
                "           s.road_section_id = (SELECT id FROM ROAD_SECTION WHERE natural_id = :roadSectionNumber and road_id = (SELECT id FROM ROAD WHERE natural_id = :roadNumber)), " +
                "           s.x_coord_kkj3 = :xCoord, s.y_coord_kkj3 = :yCoord, s.longitude_wgs84 = :longitude, s.latitude_wgs84 = :latitude, " +
                "           s.obsolete_date = null",
                args);
    }

    public void makeNonObsoleteSitesObsolete() {
        jdbcTemplate.update("UPDATE SITE SET obsolete_date = sysdate WHERE obsolete_date IS NULL", new HashMap<>());
    }
}
