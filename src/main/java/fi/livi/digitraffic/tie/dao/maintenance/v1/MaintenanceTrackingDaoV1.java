package fi.livi.digitraffic.tie.dao.maintenance.v1;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingFeatureV1;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingLatestFeatureV1;
import fi.livi.digitraffic.tie.helper.PostgisGeometryUtils;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;

@Repository
public class MaintenanceTrackingDaoV1 {

    private static final double COORDINATE_PRECISION = 0.000001;
    private static final double SIMPLIFY_DOUGLAS_PEUCKER_TOLERANCE = 0.00005;
    private static final String MIN_TIMESTAMP = "1971-01-01T00:00Z";
    private static final String MAX_TIMESTAMP = "2300-01-01T00:00Z";

    private static final String DTO_SELECT_FIELDS_WITHOUT_LINE_STRING =
        "SELECT tracking.id\n" +
        "     , tracking.previous_tracking_id AS previousId\n" +
        "     , tracking.sending_time AS sendingTime\n" +
        "     , tracking.start_time AS startTime\n" +
        "     , tracking.end_time AS endTime\n" +
        "     , tracking.created AS created\n" +
        "     , ST_AsGeoJSON(ST_Snaptogrid(tracking.last_point, " + COORDINATE_PRECISION + ")) AS lastPointJson\n" +
        "     , tracking.direction\n" +
        "     , tracking.work_machine_id AS workMachineId\n" +
        "     , STRING_AGG(tasks.task, ',') AS tasksAsString\n" +
        "     , tracking.domain\n" +
        "     , COALESCE(contract.source, domain.source) AS source\n" +
        "     , tracking.modified\n";

    private static final String DTO_SELECT_FIELDS_WITH_LINE_STRING =
        DTO_SELECT_FIELDS_WITHOUT_LINE_STRING +
        // ST_Snaptogrid will convert linestring with only same locations ie. [ [a,b], [a,b]] to null -> returns only valid linestrings
        "     , ST_AsGeoJSON(ST_Simplify(ST_Snaptogrid(tracking.line_string, " + COORDINATE_PRECISION + "), " + SIMPLIFY_DOUGLAS_PEUCKER_TOLERANCE + ", TRUE)) AS lineStringJson\n";

    private static final String DTO_TABLES =
        "FROM maintenance_tracking tracking\n" +
        "INNER JOIN maintenance_tracking_task tasks ON tracking.id = tasks.maintenance_tracking_id\n" +
        "LEFT OUTER JOIN maintenance_tracking_domain_contract contract on (tracking.domain = contract.domain AND tracking.contract = contract.contract)\n" +
        "LEFT OUTER JOIN maintenance_tracking_domain domain on tracking.domain = domain.name\n";

    private static final String DTO_LINESTRING_SQL =
        DTO_SELECT_FIELDS_WITH_LINE_STRING +
        DTO_TABLES;

    private static final String DTO_LAST_POINT_SQL =
        DTO_SELECT_FIELDS_WITHOUT_LINE_STRING +
        DTO_TABLES;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public MaintenanceTrackingDaoV1(final NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = namedParameterJdbcTemplate;
    }

    final static String AREA_POINT_QUERY = "  AND ST_INTERSECTS(ST_SetSRID(ST_GeomFromText(:area), 4326), t.last_point) = TRUE";
    final static String AREA_QUERY = "  AND (ST_INTERSECTS(ST_SetSRID(ST_GeomFromText(:area), 4326), tracking.last_point) = TRUE OR \n" +
                                     "       ST_INTERSECTS(ST_SetSRID(ST_GeomFromText(:area), 4326), tracking.line_string) = TRUE)";

    final String TASK_QUERY = "  AND (\n" +
        "    EXISTS (\n" +
        "      SELECT 1\n" +
        "      FROM maintenance_tracking_task t\n" +
        "      WHERE t.maintenance_tracking_id = tracking.id\n" +
        "        AND t.task IN (:tasks)\n" +
        "    )\n" +
        "  )";

    final static String TRACKINGS_SQL =
        DTO_LINESTRING_SQL +
        "WHERE cast(coalesce(cast(:endFrom AS TEXT), '" + MIN_TIMESTAMP + "') as TIMESTAMP) <= tracking.end_time\n" + // inclusive
        "  AND tracking.end_time < cast(coalesce(cast(:endBefore AS TEXT), '" + MAX_TIMESTAMP + "') as TIMESTAMP)\n" + // exclusive
        "  AND cast(coalesce(cast(:createdAfter AS TEXT), '" + MIN_TIMESTAMP + "') as TIMESTAMP) < tracking.created \n" + // exclusive
        "  AND tracking.created < cast(coalesce(cast(:createdBefore AS TEXT), '" + MAX_TIMESTAMP + "') as TIMESTAMP)\n" + // exclusive
        "  AREA_QUERY\n" +
        "  TASK_QUERY\n" +
        "  AND tracking.domain IN (:domains)\n" +
        "  AND domain.source IS NOT NULL\n" +
        "GROUP BY tracking.id, contract.source, domain.source\n" +
        "ORDER BY tracking.id";


    final static String TRACKINGS_LAST_POINTS_SQL = DTO_LAST_POINT_SQL +
        "WHERE tracking.id IN (\n" +
        "    SELECT max(t.id)\n" + // select latest id per machine
        "    FROM maintenance_tracking t\n" +
        "    WHERE (t.end_time BETWEEN :endFrom AND :endTo)\n" +
        "    AREA_POINT_QUERY\n" +
        "    GROUP BY t.work_machine_id\n" +
        "  )\n" +
        "  TASK_QUERY\n" +
        "  AND tracking.domain IN (:domains) \n" +
        "  AND domain.source IS NOT NULL\n" +
        "GROUP BY tracking.id, contract.source, domain.source\n" +
        "ORDER by tracking.id";

    public List<MaintenanceTrackingFeatureV1> findByAgeAndBoundingBoxAndTasks(final Instant endFrom, final Instant endBefore,
                                                                              final Instant createdAfter, final Instant createdBefore,
                                                                              final Geometry area, final List<String> tasks, final List<String> domains) {

        final MapSqlParameterSource paramSource = new MapSqlParameterSource()
            .addValue("endFrom", toTimestamp(endFrom), Types.TIMESTAMP)
            .addValue("endBefore", toTimestamp(endBefore), Types.TIMESTAMP)
            .addValue("createdAfter", toTimestamp(createdAfter), Types.TIMESTAMP)
            .addValue("createdBefore", toTimestamp(createdBefore), Types.TIMESTAMP)
            .addValue("domains", domains);

        final boolean areaSet = area != null;
        final boolean tasksSet = tasks != null && !tasks.isEmpty();

        if (areaSet) {
            paramSource.addValue("area", area.toText());
        }
        if (tasksSet) {
            paramSource.addValue("tasks", tasks);
        }

        final String QUERY_SQL =
            TRACKINGS_SQL
                .replace("AREA_QUERY", areaSet ? AREA_QUERY : "")
                .replace("TASK_QUERY", tasksSet ? TASK_QUERY : "");

        return jdbcTemplate.query(QUERY_SQL, paramSource, (rs, rowNum) -> {

                final Instant modified = rs.getObject("modified", OffsetDateTime.class).toInstant();

                return new MaintenanceTrackingFeatureV1(
                    convertToGeoJSONGeometry(rs.getString("lineStringJson"), rs.getString("lastPointJson")),
                    new fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingPropertiesV1(
                        rs.getLong("id"),
                        rs.getObject("previousId", Long.class),
                        rs.getObject("sendingTime", OffsetDateTime.class).toInstant(),
                        rs.getObject("startTime", OffsetDateTime.class).toInstant(),
                        rs.getObject("endTime", OffsetDateTime.class).toInstant(),
                        rs.getObject("created", OffsetDateTime.class).toInstant(),
                        Arrays.stream(rs.getString("tasksAsString").split(",")).map(MaintenanceTrackingTask::valueOf).collect(Collectors.toSet()),
                        rs.getBigDecimal("direction"),
                        rs.getString("domain"),
                        rs.getString("source"),
                        modified)
                    );
            });
    }


    public List<MaintenanceTrackingLatestFeatureV1> findLatestByAgeAndBoundingBoxAndTasks(final Instant endFrom, final Instant endTo, final Geometry area,
                                                                                          final List<String> tasks, final List<String> domains) {

        final MapSqlParameterSource paramSource = new MapSqlParameterSource()
            .addValue("endFrom", toTimestamp(endFrom), Types.TIMESTAMP)
            .addValue("endTo", toTimestamp(endTo), Types.TIMESTAMP)
            .addValue("domains", domains);

        final boolean areaSet = area != null;
        final boolean tasksSet = tasks != null && !tasks.isEmpty();

        if (areaSet) {
            paramSource.addValue("area", area.toText());
        }
        if (tasksSet) {
            paramSource.addValue("tasks", tasks);
        }

        final String QUERY_SQL =
            TRACKINGS_LAST_POINTS_SQL
                .replace("AREA_POINT_QUERY", areaSet ? AREA_POINT_QUERY : "")
                .replace("TASK_QUERY", tasksSet ? TASK_QUERY : "");


        return jdbcTemplate.query(QUERY_SQL, paramSource, (rs, rowNum) -> {

                final Instant modified = rs.getObject("modified", OffsetDateTime.class).toInstant();

                return new MaintenanceTrackingLatestFeatureV1(
                    PostgisGeometryUtils.convertGeoJSONStringToGeoJSON(rs.getString("lastPointJson")),
                    new fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingLatestPropertiesV1(
                        rs.getLong("id"),
                        rs.getObject("endTime", OffsetDateTime.class).toInstant(),
                        rs.getObject("created", OffsetDateTime.class).toInstant(),
                        Arrays.stream(rs.getString("tasksAsString").split(",")).map(MaintenanceTrackingTask::valueOf).collect(Collectors.toSet()),
                        rs.getBigDecimal("direction"),
                        rs.getString("domain"),
                        rs.getString("source"),
                        modified)
                );
            });
    }

    private static fi.livi.digitraffic.tie.metadata.geojson.Geometry<?> convertToGeoJSONGeometry(final String geoJsonLineString, final String geoJsonPoint) {
        if (geoJsonLineString == null) {
            return PostgisGeometryUtils.convertGeoJSONStringToGeoJSON(geoJsonPoint);
        } else {
            final fi.livi.digitraffic.tie.metadata.geojson.Geometry<?> lineString =
                PostgisGeometryUtils.convertGeoJSONStringToGeoJSON(geoJsonLineString);
            return lineString != null && lineString.getCoordinates().size() > 1 ?
                    lineString :
                    PostgisGeometryUtils.convertGeoJSONStringToGeoJSON(geoJsonPoint);
        }
    }

    private Timestamp toTimestamp(final Instant time) {
        return time == null ? null : Timestamp.from(time);
    }

}