package fi.livi.digitraffic.tie.dao.v2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.helper.DaoUtils;
import fi.livi.digitraffic.tie.helper.PostgisGeometryUtils;
import fi.livi.digitraffic.tie.metadata.geojson.MultiLineString;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionV2Feature;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionV2Properties;
import fi.livi.digitraffic.tie.model.v1.forecastsection.RoadSegment;
import fi.livi.digitraffic.tie.service.v1.forecastsection.dto.v2.ForecastSectionV2FeatureDto;
import fi.livi.digitraffic.tie.service.v1.forecastsection.dto.v2.RoadSegmentDto;

@Repository
public class V2ForecastSectionMetadataDao {
    private static final Logger log = LoggerFactory.getLogger(V2ForecastSectionMetadataDao.class);

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String INSERT_FORECAST_SECTION =
        "INSERT INTO forecast_section(id, natural_id, description, length, version, geometry, geometry_simplified) " +
        "VALUES(nextval('seq_forecast_section'), :naturalId, :description, :length, :version, " +
        "       ST_Force3D(ST_SetSRID(ST_GeomFromText(:geometry), 4326)), ST_Force3D(ST_SetSRID(ST_GeomFromText(:geometrySimplified), 4326))) " +
        "ON CONFLICT ON CONSTRAINT forecast_section_unique " +
        "DO NOTHING ";

    private static final String UPDATE_FORECAST_SECTION =
        "UPDATE forecast_section SET description = :description, length = :length, " +
        " geometry = ST_Force3D(ST_SetSRID(ST_GeomFromText(:geometry), 4326)), " +
        " geometry_simplified = ST_Force3D(ST_SetSRID(ST_GeomFromText(:geometrySimplified), 4326))" +
        "WHERE natural_id = :naturalId AND version = :version AND obsolete_date IS null";

    private static final String SELECT_ALL =
        "SELECT rs.order_number as rs_order_number, " +
        "rs.start_distance as rs_start_distance, " +
        "rs.end_distance as rs_end_distance, " +
        "rs.carriageway as rs_carriageway," +
        "li.order_number as li_order_number, " +
        "f.natural_id as natural_id, f.id as forecast_section_id, description, road_number, road_section_number, length, link_id, " +
        "ST_AsBinary(f.geometry) as geometry\n" + // WKB
        "FROM forecast_section f\n" +
        "LEFT OUTER JOIN road_segment rs ON rs.forecast_section_id = f.id\n" +
        "LEFT OUTER JOIN link_id li ON li.forecast_section_id = f.id\n" +
        "WHERE f.version = 2\n" +
        "AND (:roadNumber IS NULL OR f.road_number::integer = :roadNumber)\n" +
        "AND (:naturalIdsIsEmpty IS TRUE OR f.natural_id IN (:naturalIds))\n" +
        "INTERSECTS_AREA" +
        "ORDER BY f.natural_id";

    private static final String INTERSECTS_AREA  = "AND ST_INTERSECTS(ST_SetSRID(ST_GeomFromText(:area), 4326), f.geometry) = TRUE\n";
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
        args.put("naturalId", feature.getProperties().getId());
        args.put("description", feature.getProperties().getDescription());
        args.put("length", feature.getProperties().getTotalLengthKm() * 1000);
        args.put("version", 2);

        try {
            final String json = feature.getGeometry().toJsonString();
            final Geometry geometry = PostgisGeometryUtils.convertGeoJsonGeometryToGeometry(json);
            final Geometry simplifiedGeometry = PostgisGeometryUtils.simplify(geometry);
            args.put("geometry", geometry.toText());
            args.put("geometrySimplified", simplifiedGeometry.toText());
        } catch (final Exception e) {
            log.error("Failed to convert ForecastSectionV2FeatureDto geometry", e);
        }
        return new MapSqlParameterSource(args);
    }

    public List<ForecastSectionV2Feature> findForecastSectionV2Features(final Integer roadNumber, final Double minLongitude, final Double minLatitude,
                                                                        final Double maxLongitude, final Double maxLatitude,
                                                                        final List<String> naturalIds) {
        final HashMap<String, ForecastSectionV2Feature> featureMap = new HashMap<>();

        final MapSqlParameterSource paramSource = new MapSqlParameterSource()
            .addValue("roadNumber", roadNumber, Types.INTEGER)
            .addValue("naturalIdsIsEmpty", naturalIds == null || naturalIds.isEmpty())
            .addValue("naturalIds", naturalIds);

        final String wktPolygon = PostgisGeometryUtils.convertBoundsCoordinatesToWktPolygon(minLongitude, maxLongitude, minLatitude, maxLatitude);
        final String selectSql = SELECT_ALL.replace("INTERSECTS_AREA",
                                                    wktPolygon != null ? INTERSECTS_AREA : "");
        if (wktPolygon != null) {
            paramSource.addValue("area", wktPolygon, Types.VARCHAR);
        }

        jdbcTemplate.query(selectSql, paramSource, rs -> {
            final String naturalId = rs.getString("natural_id");

            featureMap.computeIfAbsent(naturalId, s -> convert(naturalId, rs));

            setRoadSegment(rs, featureMap.get(naturalId));
            setLinkId(rs, featureMap.get(naturalId));
        });

        return featureMap.values().stream()
            .sorted(Comparator.comparing(f -> f.getProperties().getNaturalId())).collect(Collectors.toList());
    }

    private ForecastSectionV2Feature convert(final String naturalId, final ResultSet rs) {
        try {
            final byte[] wkbBytes = rs.getBytes("geometry");
            final Geometry g = PostgisGeometryUtils.convertWKBToGeometry(wkbBytes);
            final MultiLineString multiLineString = PostgisGeometryUtils.convertToGeoJSONMultiLineLineString(g);
            return new ForecastSectionV2Feature(rs.getLong("forecast_section_id"),
                multiLineString,
                new ForecastSectionV2Properties(naturalId,
                    rs.getString("description"),
                    Integer.parseInt(rs.getString("road_number")),
                    Integer.parseInt(rs.getString("road_section_number")),
                    rs.getInt("length"),
                    new ArrayList<>(),
                    new ArrayList<>()));
        } catch (final SQLException e) {
            log.error("method=convert SQLException naturalId: " + naturalId, e);
            return null;
        } catch (final ParseException e) {
            log.error("method=convert Geometry convert exception naturalId: " + naturalId, e);
            return null;
        }
    }

    private static void setLinkId(final ResultSet rs, final ForecastSectionV2Feature feature) throws SQLException {
        final int orderNumber = rs.getInt("li_order_number");

        while (feature.getProperties().getLinkIdList().size() < orderNumber) {
            feature.getProperties().getLinkIdList().add(0L);
        }

        feature.getProperties().getLinkIdList().set(orderNumber - 1, rs.getLong("link_id"));
    }

    private static void setRoadSegment(final ResultSet rs, final ForecastSectionV2Feature feature) throws SQLException {
        final int orderNumber = rs.getInt("rs_order_number");

        while (feature.getProperties().getRoadSegments().size() < orderNumber) {
            feature.getProperties().getRoadSegments().add(new RoadSegment());
        }
        final RoadSegment roadSegment = feature.getProperties().getRoadSegments().get(orderNumber - 1);

        roadSegment.setStartDistance(rs.getInt("rs_start_distance"));
        roadSegment.setEndDistance(rs.getInt("rs_end_distance"));
        roadSegment.setCarriageway(DaoUtils.findInteger(rs, "rs_carriageway"));
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
