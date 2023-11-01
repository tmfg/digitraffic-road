package fi.livi.digitraffic.tie.dao.weather.forecast;

import java.util.HashMap;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.weather.forecast.client.ForecastSectionV2FeatureDto;
import fi.livi.digitraffic.tie.dto.weather.forecast.client.RoadSegmentDto;
import fi.livi.digitraffic.tie.helper.ForecastSectionNaturalIdHelper;
import fi.livi.digitraffic.tie.helper.GeometryConstants;
import fi.livi.digitraffic.tie.helper.PostgisGeometryUtils;

@Repository
public class V2ForecastSectionMetadataDao {
    private static final Logger log = LoggerFactory.getLogger(V2ForecastSectionMetadataDao.class);

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String INSERT_FORECAST_SECTION =
        "INSERT INTO forecast_section(id, natural_id, description, length, version, geometry, geometry_simplified," +
        "                             road_number, road_section_number, road_section_version_number) " +
        "VALUES(nextval('seq_forecast_section'), :naturalId, :description, :length, :version, " +
        "       ST_Force3D(ST_SetSRID(ST_GeomFromText(:geometry), " + GeometryConstants.SRID + ")), ST_Force3D(ST_SetSRID(ST_GeomFromText(:geometrySimplified), " + GeometryConstants.SRID + "))," +
        "       :roadNumber, :roadSectionNumber, :roadSectionVersionNumber)" +
        "ON CONFLICT ON CONSTRAINT forecast_section_unique " +
        "DO NOTHING ";

    private static final String UPDATE_FORECAST_SECTION =
        "UPDATE forecast_section SET " +
        "  description = :description," +
        "  length = :length," +
        "  geometry = ST_Force3D(ST_SetSRID(ST_GeomFromText(:geometry), " + GeometryConstants.SRID + "))," +
        "  geometry_simplified = ST_Force3D(ST_SetSRID(ST_GeomFromText(:geometrySimplified), " + GeometryConstants.SRID + "))," +
        "  road_number = :roadNumber," +
        "  road_section_number = :roadSectionNumber," +
        "  road_section_version_number = :roadSectionVersionNumber " +
        "WHERE natural_id = :naturalId AND version = :version AND obsolete_date IS null";

    private static final String INSERT_ROAD_SEGMENT =
        "INSERT INTO road_segment(forecast_section_id, order_number, start_distance, end_distance, carriageway) " +
        "VALUES((SELECT id FROM forecast_section WHERE natural_id = :naturalId), :orderNumber, :startDistance, :endDistance, :carriageway)";

    private static final String INSERT_LINK_IDS =
        "INSERT INTO link_id(forecast_section_id, order_number, link_id) " +
        "VALUES((SELECT id FROM forecast_section WHERE natural_id = :naturalId), :orderNumber, :linkId)";

    @Autowired
    public V2ForecastSectionMetadataDao(final NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = namedParameterJdbcTemplate;
    }

    public void upsertForecastSections(final List<ForecastSectionV2FeatureDto> features) {
        final MapSqlParameterSource[] sources = new MapSqlParameterSource[features.size()];
        int i = 0;
        for (final ForecastSectionV2FeatureDto feature : features) {
            sources[i] = forecastSectionParameterSource(feature);
            i++;
        }

        // Only DO NOTHING is supported for EXCLUDE constraint (ON CONFLICT ON CONSTRAINT forecast_section_unique)
        jdbcTemplate.batchUpdate(INSERT_FORECAST_SECTION, sources);
        jdbcTemplate.batchUpdate(UPDATE_FORECAST_SECTION, sources);
    }

    private static MapSqlParameterSource forecastSectionParameterSource(final ForecastSectionV2FeatureDto feature) {
        final HashMap<String, Object> args = new HashMap<>();
        final String naturalId = feature.getProperties().getId();
        args.put("naturalId", naturalId);
        args.put("description", feature.getProperties().getDescription());
        args.put("length", feature.getProperties().getTotalLengthKm() * 1000);
        args.put("version", 2);

        args.put("roadNumber", ForecastSectionNaturalIdHelper.getRoadNumber(naturalId));
        args.put("roadSectionNumber", ForecastSectionNaturalIdHelper.getRoadSectionNumber(naturalId));
        args.put("roadSectionVersionNumber", 0);

        try {
            final String json = feature.getGeometry().toJsonString();
            final Geometry geometry = PostgisGeometryUtils.convertGeoJsonGeometryToGeometry(json);
            final Geometry simplifiedGeometry = PostgisGeometryUtils.snapToGrid(PostgisGeometryUtils.simplify(geometry));
            args.put("geometry", geometry.toText());
            args.put("geometrySimplified", simplifiedGeometry.toText());
        } catch (final Exception e) {
            log.error("Failed to convert ForecastSectionV2FeatureDto geometry", e);
        }
        return new MapSqlParameterSource(args);
    }

    public void insertRoadSegments(final List<ForecastSectionV2FeatureDto> features) {
        final MapSqlParameterSource[] segmentSources = new MapSqlParameterSource[features.stream().mapToInt(f -> f.getProperties().getRoadSegmentList().size()).sum()];

        int i = 0;
        for (final ForecastSectionV2FeatureDto feature : features) {
            int orderNumber = 1;
            for (final RoadSegmentDto roadSegmentDto : feature.getProperties().getRoadSegmentList()) {
                segmentSources[i] = roadSegmentParameterSource(feature, roadSegmentDto, orderNumber);
                i++;
                orderNumber++;
            }
        }

        jdbcTemplate.batchUpdate(INSERT_ROAD_SEGMENT, segmentSources);
    }

    private static MapSqlParameterSource roadSegmentParameterSource(final ForecastSectionV2FeatureDto feature, final RoadSegmentDto roadSegmentDto, final int orderNumber) {
        return new MapSqlParameterSource()
            .addValue("naturalId", feature.getProperties().getId())
            .addValue("orderNumber", orderNumber)
            .addValue("startDistance", roadSegmentDto.getStartDistance())
            .addValue("endDistance", roadSegmentDto.getEndDistance())
            .addValue("carriageway", roadSegmentDto.getCarriageway());
    }

    public void insertLinkIds(final List<ForecastSectionV2FeatureDto> features) {
        final MapSqlParameterSource[] linkIdSources = new MapSqlParameterSource[features.stream().mapToInt(f -> f.getProperties().getLinkIdList().size()).sum()];

        int i = 0;
        for (final ForecastSectionV2FeatureDto feature : features) {
            int orderNumber = 1;
            for (final Long linkId : feature.getProperties().getLinkIdList()) {
                linkIdSources[i] = linkIdParameterSource(feature, linkId, orderNumber);
                i++;
                orderNumber++;
            }
        }

        jdbcTemplate.batchUpdate(INSERT_LINK_IDS, linkIdSources);
    }

    private static MapSqlParameterSource linkIdParameterSource(final ForecastSectionV2FeatureDto feature, final Long linkId, final int orderNumber) {
        return new MapSqlParameterSource()
            .addValue("naturalId", feature.getProperties().getId())
            .addValue("orderNumber", orderNumber)
            .addValue("linkId", linkId);
    }
}
