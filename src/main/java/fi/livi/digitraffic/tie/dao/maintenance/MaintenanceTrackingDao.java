package fi.livi.digitraffic.tie.dao.maintenance;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingFeatureV1;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingLatestFeatureV1;
import fi.livi.digitraffic.tie.helper.GeometryConstants;
import fi.livi.digitraffic.tie.helper.PostgisGeometryUtils;
import fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingTask;

@Repository
public class MaintenanceTrackingDao {

    public static final String STATE_ROADS_DOMAIN = "state-roads";
    public static final String GENERIC_ALL_DOMAINS = "all";
    public static final String GENERIC_MUNICIPALITY_DOMAINS = "municipalities";

    private static final String MIN_TIMESTAMP = "1971-01-01T00:00Z";
    private static final String MAX_TIMESTAMP = "2300-01-01T00:00Z";

    private static final String DTO_SELECT_FIELDS_WITHOUT_LINE_STRING = """
        SELECT tracking.id
             , tracking.previous_tracking_id AS previousId
             , tracking.sending_time AS sendingTime
             , tracking.start_time AS startTime
             , tracking.end_time AS endTime
             , tracking.created AS created
             , ST_AsGeoJSON(tracking.last_point) AS lastPointJson
             , tracking.direction
             , tracking.work_machine_id AS workMachineId
             , ARRAY_TO_STRING(tracking.tasks, ',') AS tasksAsString
             , tracking.domain
             , COALESCE(contract.source, domain.source) AS source
             , tracking.modified
        """;

    private static final String DTO_SELECT_FIELDS_WITH_LINE_STRING =
        DTO_SELECT_FIELDS_WITHOUT_LINE_STRING +
        "     , ST_AsGeoJSON(tracking.geometry) AS geometryStringJson\n";

    private static final String DTO_TABLES = """
                    FROM maintenance_tracking tracking
                    LEFT OUTER JOIN maintenance_tracking_domain_contract contract on (tracking.domain = contract.domain AND tracking.contract = contract.contract)
                    LEFT OUTER JOIN maintenance_tracking_domain domain on tracking.domain = domain.name
                    """;

    private static final String DTO_LINESTRING_SQL =
        DTO_SELECT_FIELDS_WITH_LINE_STRING +
        DTO_TABLES;

    private static final String DTO_LAST_POINT_SQL =
        DTO_SELECT_FIELDS_WITHOUT_LINE_STRING +
        DTO_TABLES;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public MaintenanceTrackingDao(final NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = namedParameterJdbcTemplate;
    }

    final static String AREA_POINT_QUERY = "  AND (ST_SetSRID(ST_GeomFromText(:area), " + GeometryConstants.SRID + ") && t.last_point) = TRUE";
    final static String AREA_QUERY = "  AND ( (ST_SetSRID(ST_GeomFromText(:area), " + GeometryConstants.SRID + ") && tracking.geometry) = TRUE )";

    final static String CREATED_AFTER_QUERY = "  AND cast(:createdAfter as TIMESTAMP) < tracking.created"; // exclusive
    final static String CREATED_BEFORE_QUERY = "  AND tracking.created < cast(:createdBefore as TIMESTAMP)"; // exclusive

    final String TASK_QUERY = "  AND (tracking.tasks && cast( :tasks AS maintenance_task_enum[]))\n";

    final static String TRACKINGS_SQL =
        DTO_LINESTRING_SQL +
        "WHERE cast(coalesce(cast(:endFrom AS TEXT), '" + MIN_TIMESTAMP + "') as TIMESTAMP) <= tracking.end_time\n" + // inclusive
        "  AND tracking.end_time < cast(coalesce(cast(:endBefore AS TEXT), '" + MAX_TIMESTAMP + "') as TIMESTAMP)\n" + // exclusive
        "  CREATED_AFTER_QUERY\n" +
        "  CREATED_BEFORE_QUERY\n" +
        "  AREA_QUERY\n" +
        "  TASK_QUERY\n" +
        "  AND tracking.domain IN (:domains)\n" +
        "  AND domain.source IS NOT NULL\n" +
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
        "ORDER by tracking.id";

    public List<MaintenanceTrackingFeatureV1> findByAgeAndBoundingBoxAndTasks(final Instant endFrom, final Instant endBefore,
                                                                              final Instant createdAfter, final Instant createdBefore,
                                                                              final Geometry area, final Set<String> tasks, final Set<String> domains) {

        final MapSqlParameterSource paramSource = new MapSqlParameterSource()
            .addValue("endFrom", toTimestamp(endFrom), Types.TIMESTAMP)
            .addValue("endBefore", toTimestamp(endBefore), Types.TIMESTAMP)
            .addValue("domains", domains);

        if (createdBefore != null) {
            paramSource.addValue("createdBefore", toTimestamp(createdBefore), Types.TIMESTAMP);
        }
        if (createdAfter != null) {
            paramSource.addValue("createdAfter", toTimestamp(createdAfter), Types.TIMESTAMP);
        }

        final boolean areaSet = area != null;
        final boolean tasksSet = tasks != null && !tasks.isEmpty();

        if (areaSet) {
            paramSource.addValue("area", area.toText());
        }
        if (tasksSet) {
            paramSource.addValue("tasks", tasks.toArray(new String[0]));
        }

        final String QUERY_SQL =
            TRACKINGS_SQL
                .replace("AREA_QUERY", areaSet ? AREA_QUERY : "")
                .replace("TASK_QUERY", tasksSet ? TASK_QUERY : "")
                .replace("CREATED_AFTER_QUERY", createdAfter != null ? CREATED_AFTER_QUERY : "")
                .replace("CREATED_BEFORE_QUERY", createdBefore != null ? CREATED_BEFORE_QUERY : "");

        return jdbcTemplate.query(QUERY_SQL, paramSource, (rs, rowNum) -> {

                final Instant modified = rs.getObject("modified", OffsetDateTime.class).toInstant();

                return new MaintenanceTrackingFeatureV1(
                    PostgisGeometryUtils.convertGeoJSONStringToGeoJSON(rs.getString("geometryStringJson")),
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
                                                                                          final Set<String> tasks, final Set<String> domains) {

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
            paramSource.addValue("tasks", tasks.toArray(new String[0]));
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

    private Timestamp toTimestamp(final Instant time) {
        return time == null ? null : Timestamp.from(time);
    }

    public MaintenanceTrackingFeatureV1 getById(final long id) {
        final String QUERY_SQL =
            DTO_LINESTRING_SQL +
            "WHERE tracking.id = :id\n" +
            "  AND domain.source IS NOT NULL";
        final MapSqlParameterSource paramSource = new MapSqlParameterSource()
            .addValue("id", id, Types.NUMERIC);

        return jdbcTemplate.query(QUERY_SQL,paramSource, (ResultSet rs) -> {
            if (rs.next()) {

                final Instant modified = rs.getObject("modified", OffsetDateTime.class).toInstant();

                return new MaintenanceTrackingFeatureV1(
                    PostgisGeometryUtils.convertGeoJSONStringToGeoJSON(rs.getString("geometryStringJson")),
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
            }
            return null;
        });
    }
}