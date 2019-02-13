package fi.livi.digitraffic.tie.metadata.dao;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.helper.DaoUtils;
import fi.livi.digitraffic.tie.metadata.geojson.MultiLineString;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionV2Feature;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionV2Properties;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.LinkId;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.RoadSegment;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.dto.v1.Coordinate;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.dto.v2.ForecastSectionV2FeatureDto;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.dto.v2.RoadSegmentDto;

@Repository
public class ForecastSectionV2MetadataDao {

    private static final Logger log = LoggerFactory.getLogger(ForecastSectionV2MetadataDao.class);

    private final NamedParameterJdbcTemplate jdbcTemplate;

    // FIXME: forecast_section natural_id should be unique?
    private static final String insertForecastSection =
        "INSERT INTO forecast_section(id, natural_id, description, length, version) " +
        "VALUES(nextval('seq_forecast_section'), :naturalId, :description, :length, :version) " +
        "ON CONFLICT ON CONSTRAINT forecast_section_unique " +
        "DO NOTHING ";

    private static final String updateForecastSection =
        "UPDATE forecast_section SET description = :description, length = :length, obsolete_date = null " +
        "WHERE natural_id = :naturalId AND version = :version AND obsolete_date IS null";

    private static final String insertCoordinateList =
        "INSERT INTO forecast_section_coordinate_list(forecast_section_id, order_number) " +
        "VALUES((SELECT id FROM forecast_Section WHERE natural_id = :naturalId and version = :version), :orderNumber)";

    private static final String insertCoordinate =
        "INSERT INTO forecast_section_coordinate(forecast_section_id, list_order_number, order_number, longitude, latitude) " +
        "VALUES((SELECT id FROM forecast_section WHERE natural_id = :naturalId), :listOrderNumber, :orderNumber, :longitude, :latitude)";

    private static final String selectAll =
        "SELECT rs.order_number as rs_order_number, " +
        "rs.start_distance as rs_start_distance, rs.end_distance as rs_end_distance, rs.carriageway as rs_carriageway," +
        "li.order_number as li_order_number, * " +
        "FROM forecast_section f " +
        "          LEFT OUTER JOIN road_segment rs ON rs.forecast_section_id = f.id\n" +
        "          LEFT OUTER JOIN link_id li ON li.forecast_section_id = f.id\n" +
        "WHERE f.version = 2\n" +
        "ORDER BY f.natural_id";

    private static final String selectCoordinates =
        "SELECT f.natural_id, c.list_order_number, '[' || array_to_string(array_agg('['|| longitude ||','|| latitude ||']' ORDER BY order_number), ',') || ']' AS coordinates\n" +
        "FROM forecast_section_coordinate c INNER JOIN forecast_section f ON c.forecast_section_id = f.id\n" +
        "WHERE f.version = 2\n" +
        "GROUP BY natural_id, forecast_section_id, list_order_number\n" +
        "ORDER BY natural_id, forecast_section_id, list_order_number";

    private static final String insertRoadSegment =
        "INSERT INTO road_segment(forecast_section_id, order_number, start_distance, end_distance, carriageway) " +
        "VALUES((SELECT id FROM forecast_section WHERE natural_id = :naturalId), :orderNumber, :startDistance, :endDistance, :carriageway)";

    private static final String insertLinkIds =
        "INSERT INTO link_id(forecast_section_id, order_number, link_id) " +
        "VALUES((SELECT id FROM forecast_section WHERE natural_id = :naturalId), :orderNumber, :linkId)";

    @Autowired
    public ForecastSectionV2MetadataDao(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void upsertForecastSections(final List<ForecastSectionV2FeatureDto> features) {

        final MapSqlParameterSource sources[] = new MapSqlParameterSource[features.size()];
        int i = 0;
        for (final ForecastSectionV2FeatureDto feature : features) {
            sources[i] = forecastSectionParameterSource(feature);
            i++;
        }

        // Only DO NOTHING is supported for EXCLUDE constraint (ON CONFLICT ON CONSTRAINT forecast_section_unique)
        jdbcTemplate.batchUpdate(insertForecastSection, sources);
        jdbcTemplate.batchUpdate(updateForecastSection, sources);
    }

    private static MapSqlParameterSource forecastSectionParameterSource(final ForecastSectionV2FeatureDto feature) {
        final HashMap<String, Object> args = new HashMap<>();
        args.put("naturalId", feature.getProperties().getId());
        args.put("description", feature.getProperties().getDescription());
        args.put("length", feature.getProperties().getTotalLengthKm() * 1000);
        args.put("version", 2);
        return new MapSqlParameterSource(args);
    }

    public void insertCoordinates(final List<ForecastSectionV2FeatureDto> features) {

        final int listCount = features.stream().mapToInt(f -> f.getGeometry().getCoordinates().size()).sum();
        final MapSqlParameterSource listSources[] = new MapSqlParameterSource[listCount];

        final int coordinateCount = features.stream().mapToInt(f -> f.getGeometry().getCoordinates().stream().mapToInt(l -> l.size()).sum()).sum();
        final MapSqlParameterSource coordinateSources[] = new MapSqlParameterSource[coordinateCount];

        int listNumber = 0;
        int coordinateNumber = 0;
        for (final ForecastSectionV2FeatureDto feature : features) {

            int listOrderNumber = 1;
            for (final List<Coordinate> coordinates : feature.getGeometry().getCoordinates()) {
                listSources[listNumber] = coordinateListParameterSource(feature.getProperties().getId(), listOrderNumber);

                int coordinateOrderNumber = 1;
                for (final Coordinate coordinate : coordinates) {
                    coordinateSources[coordinateNumber] = coordinateParameterSource(feature.getProperties().getId(), listOrderNumber, coordinateOrderNumber, coordinate);
                    coordinateOrderNumber++;
                    coordinateNumber++;
                }
                listNumber++;
                listOrderNumber++;
            }
        }

        jdbcTemplate.batchUpdate(insertCoordinateList, listSources);
        jdbcTemplate.batchUpdate(insertCoordinate, coordinateSources);
    }

    public List<ForecastSectionV2Feature> findForecastSectionV2Features() {
        jdbcTemplate.getJdbcTemplate().setFetchSize(1000);
        final HashMap<String, ForecastSectionV2Feature> featureMap = new HashMap<>();

        jdbcTemplate.query(selectAll, rs -> {
            final String naturalId = rs.getString("natural_id");

            if (!featureMap.containsKey(naturalId)) {
                final ForecastSectionV2Feature feature = new ForecastSectionV2Feature(rs.getLong("forecast_section_id"),
                                                                                      new MultiLineString(),
                                                                                      new ForecastSectionV2Properties(naturalId,
                                                                                                                      rs.getString("description"),
                                                                                                                      Integer.parseInt(rs.getString("road_number")),
                                                                                                                      Integer.parseInt(rs.getString("road_section_number")),
                                                                                                                      rs.getInt("length"),
                                                                                                                      new ArrayList<>(),
                                                                                                                      new ArrayList<>()));
                setRoadSegment(rs, feature);
                setLinkId(rs, feature);

                featureMap.put(naturalId, feature);
            } else {
                setRoadSegment(rs, featureMap.get(naturalId));
                setLinkId(rs, featureMap.get(naturalId));
            }
        });

        jdbcTemplate.query(selectCoordinates, rs -> {
            final TypeReference<List<List<Double>>> typeReference = new TypeReference<List<List<Double>>>() {};
            List coordinates = new ArrayList();
            try {
                coordinates = new ObjectMapper().readValue(rs.getString("coordinates"), typeReference);
            } catch (IOException e) {
                log.error("method=findForecastSectionV2Features coordinates objectMapper readValue error");
            }
            final ForecastSectionV2Feature feature = featureMap.get(rs.getString("natural_id"));
            feature.getGeometry().coordinates.add(coordinates);
        });

        return featureMap.values().stream()
            .sorted(Comparator.comparing(f -> f.getProperties().getNaturalId())).collect(Collectors.toList());
    }

    private static void setLinkId(final ResultSet rs, final ForecastSectionV2Feature feature) throws SQLException {
        final int orderNumber = rs.getInt("li_order_number");

        while (feature.getProperties().getLinkIdList().size() < orderNumber) {
            feature.getProperties().getLinkIdList().add(new LinkId());
        }

        final LinkId linkId = feature.getProperties().getLinkIdList().get(orderNumber - 1);
        linkId.setLinkId(rs.getLong("link_id"));
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

    private static void setCoordinate(final ResultSet rs, final ForecastSectionV2Feature feature) throws SQLException {
        final int listOrderNumber = rs.getInt("list_order_number");

        while (feature.getGeometry().coordinates.size() < listOrderNumber) {
            feature.getGeometry().coordinates.add(new ArrayList<>());
        }

        final List<List<Double>> list = feature.getGeometry().coordinates.get(listOrderNumber - 1);

        final int coordinateOrderNumber = rs.getInt("c_order_number");

        while (list.size() < coordinateOrderNumber) {
            list.add(new ArrayList<>());
        }

        list.set(coordinateOrderNumber - 1, Arrays.asList(rs.getDouble("longitude"), rs.getDouble("latitude")));
    }

    private MapSqlParameterSource coordinateParameterSource(final String naturalId, final int listOrderNumber, final int coordinateOrderNumber,
                                                            final Coordinate coordinate) {
        final HashMap<String, Object> args = new HashMap<>();
        args.put("naturalId", naturalId);
        args.put("listOrderNumber", listOrderNumber);
        args.put("orderNumber", coordinateOrderNumber);
        args.put("longitude", coordinate.longitude);
        args.put("latitude", coordinate.latitude);
        return new MapSqlParameterSource(args);
    }

    private static MapSqlParameterSource coordinateListParameterSource(final String naturalId, final int orderNumber) {
        final HashMap<String, Object> args = new HashMap<>();
        args.put("naturalId", naturalId);
        args.put("orderNumber", orderNumber);
        args.put("version", 2);
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

        jdbcTemplate.batchUpdate(insertRoadSegment, segmentSources);
    }

    private MapSqlParameterSource roadSegmentParameterSource(final ForecastSectionV2FeatureDto feature, final RoadSegmentDto roadSegmentDto, final int orderNumber) {
        final HashMap<String, Object> args = new HashMap<>();
        args.put("naturalId", feature.getProperties().getId());
        args.put("orderNumber", orderNumber);
        args.put("startDistance", roadSegmentDto.getStartDistance());
        args.put("endDistance", roadSegmentDto.getEndDistance());
        args.put("carriageway", roadSegmentDto.getCarriageway());
        return new MapSqlParameterSource(args);
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

        jdbcTemplate.batchUpdate(insertLinkIds, linkIdSources);
    }

    private MapSqlParameterSource linkIdParameterSource(final ForecastSectionV2FeatureDto feature, final Long linkId, final int orderNumber) {
        final HashMap<String, Object> args = new HashMap<>();
        args.put("naturalId", feature.getProperties().getId());
        args.put("orderNumber", orderNumber);
        args.put("linkId", linkId);
        return new MapSqlParameterSource(args);
    }
}
