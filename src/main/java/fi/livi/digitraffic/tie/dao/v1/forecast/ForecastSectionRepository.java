package fi.livi.digitraffic.tie.dao.v1.forecast;

import java.time.Instant;
import java.util.List;

import javax.persistence.QueryHint;

import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.v1.forecastsection.ForecastSection;

@Repository
public interface ForecastSectionRepository extends JpaRepository<ForecastSection, Long> {

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    @EntityGraph(attributePaths = { "road", "startRoadSection", "endRoadSection" },
                 type = EntityGraph.EntityGraphType.LOAD)
    List<ForecastSection> findDistinctByVersionIsAndObsoleteDateIsNullOrderByNaturalIdAsc(final int version);

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="400"))
    @Query(value =
        "select f\n" +
        "from ForecastSection f\n" +
        "where f.version = 1\n" +
        " AND (:roadNumber IS NULL OR f.roadNumber = :roadNumber)\n" +
        " AND (:area IS NULL OR ST_INTERSECTS(:area, f.geometry) = TRUE)\n" +
        " AND ( :#{#naturalIds.size()} = 0 OR f.naturalId IN (:naturalIds))\n" +
        "  AND f.obsoleteDate IS NULL\n" +
"order by f.naturalId")
    List<ForecastSection> findForecastSectionsV1OrderByNaturalIdAsc(final Integer roadNumber,
                                                                    final Geometry area,
                                                                    final List<String> naturalIds
    );

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="10000"))
    @Query(value =
        "with link_ids as (\n" +
        "    select li.forecast_section_id, string_agg(cast(li.link_id as text), ',' ORDER BY li.order_number) as linkIdsAsString\n" +
        "    from link_id li\n" +
        "    group by li.forecast_section_id\n" +
        "), road_segments as (\n" +
        "    select rs.forecast_section_id, cast(json_agg(json_build_object('startDistance', rs.start_distance, 'endDistance', rs.end_distance, 'carriageway', rs.carriageway) order by rs.order_number) as text) as roadSegmentsAsJsonString\n" +
        "    from road_segment rs\n" +
        "    group by rs.forecast_section_id\n" +
        ")\n" +
        "select f.natural_id as naturalId\n" +
        "     , f.description as description\n" +
        "     , f.length as length\n" +
        "     , f.road_number as roadNumber\n" +
        "     , f.road_section_number as roadSectionNumber\n" +
        "     , f.modified as modified\n" +
        "     , ST_AsGeoJSON(f.geometry) as geometryAsGeoJsonString\n" +
        "     , li.linkIdsAsString\n" +
        "     , rs.roadSegmentsAsJsonString\n" +
        "from forecast_section f\n" +
        "left outer join link_ids li on f.id = li.forecast_section_id\n" +
        "left outer join road_segments rs on f.id = rs.forecast_section_id\n" +
        "where f.version = 2\n" +
        "  AND (cast(:area as geometry) IS NULL OR ST_INTERSECTS(cast(:area as geometry), f.geometry) = TRUE)\n" +
        "  AND (cast(:roadNumber as numeric) IS NULL OR f.road_number = cast(:roadNumber as numeric))\n" +
        "  AND (coalesce(array_length(cast('{' || :naturalIds || '}' as varchar[]), 1), 0) = 0 OR f.natural_id IN (:naturalIds))\n" +
        "  AND obsolete_date IS NULL\n" +
        "order by f.natural_id",
        nativeQuery = true)
    List<ForecastSectionDto> findForecastSectionsV2OrderByNaturalIdAsc(final Integer roadNumber,
                                                                       final Geometry area,
                                                                       final List<String> naturalIds);

    @Modifying
    @Query(value = "DELETE FROM road_segment WHERE forecast_section_id IN " +
                   "(SELECT forecast_section_id FROM forecast_section WHERE version = :version AND obsolete_date IS NULL)",
           nativeQuery = true)
    void deleteRoadSegments(@Param("version") final int version);

    @Modifying
    @Query(value = "DELETE FROM link_id WHERE forecast_section_id IN (SELECT forecast_section_id FROM forecast_section WHERE version = :version)",
           nativeQuery = true)
    void deleteLinkIds(@Param("version") final int version);

    @Modifying
    @Query(value = "DELETE FROM forecast_section WHERE natural_id NOT IN (:naturalIds) AND version = :version", nativeQuery = true)
    void deleteAllNotIn(@Param("naturalIds") final List<String> naturalIds, @Param("version") final int version);

    @Query(value =
        "with max_with_filter as (\n" +
        "    SELECT max(f.modified) as modified\n" +
        "    FROM forecast_section f\n" +
        "    WHERE version = :version\n" +
        "      AND (cast(:area as geometry) IS NULL OR ST_INTERSECTS(cast(:area as geometry), f.geometry) = TRUE)\n" +
        "      AND (coalesce(array_length(cast('{' || :naturalIds || '}' as varchar[]), 1), 0) = 0 OR f.natural_id IN (:naturalIds))\n" +
        "), max_from_all as (\n" +
        "    SELECT max(f.modified) as modified FROM forecast_section f WHERE version = :version\n" +
        ")\n" +
        "select coalesce(max_with_filter.modified, max_from_all.modified)\n" +
        "from max_with_filter, max_from_all",
        nativeQuery = true) // don't filter with "obsolete_date IS NULL" as if updated to obsoleted -> data is modified
    Instant getLastModified(final int version,
                            final Geometry area,
                            final List<String> naturalIds);

}
