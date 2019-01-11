package fi.livi.digitraffic.tie.metadata.dao;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.service.forecastsection.dto.v1.Coordinate;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.dto.v2.ForecastSectionV2FeatureDto;

@Repository
public class ForecastSectionV2MetadataDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

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
        "VALUES((SELECT id FROM forecast_Section WHERE natural_id = :naturalId), :orderNumber)";

    private static final String insertCoordinate =
        "INSERT INTO forecast_section_coordinate(forecast_section_id, list_order_number, order_number, longitude, latitude) " +
        "VALUES((SELECT id FROM forecast_section WHERE natural_id = :naturalId), :listOrderNumber, :orderNumber, :longitude, :latitude)";

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
        return new MapSqlParameterSource(args);
    }
}
