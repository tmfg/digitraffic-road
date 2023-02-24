package fi.livi.digitraffic.tie.dao.v1;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.info.v1.DataSourceInfoDtoV1;
import fi.livi.digitraffic.tie.model.DataSource;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v1.DataUpdated;

@Repository
public interface DataUpdatedRepository extends JpaRepository<DataUpdated, Long> {

    String UNSET_SUBTYPE = "-";

    @Query(value = "select transaction_timestamp()", nativeQuery = true)
    Instant getTransactionStartTime();

    default Instant findUpdatedTime(final DataType dataType) {
        return findUpdatedTime(dataType, Collections.singletonList(UNSET_SUBTYPE));
    }

    @Query("SELECT max(d.updated)\n" +
           "FROM DataUpdated d\n" +
           "WHERE d.dataType = :dataType" +
           "  AND d.subtype in (:subtypes)")
    Instant findUpdatedTime(final DataType dataType, final List<String> subtypes);

    @Modifying
    default void upsertDataUpdated(final DataType dataType) {
        upsertDataUpdated(dataType, UNSET_SUBTYPE);
    }

    @Modifying
    default void upsertDataUpdated(final DataType dataType, final Instant updated) {
        upsertDataUpdated(dataType, UNSET_SUBTYPE, updated);
    }

    @Modifying
    @Query(value =
           "INSERT INTO data_updated(id, data_type, subtype, updated)\n" +
           "  VALUES(NEXTVAL('seq_data_updated'), :#{#dataType.name()}, :subtype, now())\n" +
           "  ON CONFLICT (data_type, subtype)\n" +
           "  DO UPDATE SET updated = now()", nativeQuery = true)
    void upsertDataUpdated(final DataType dataType, final String subtype);

    @Modifying
    @Query(value =
           "INSERT INTO data_updated(id, data_type, subtype, updated)\n" +
           "  VALUES(NEXTVAL('seq_data_updated'), :#{#dataType.name()}, :subtype, :updated)\n" +
           "  ON CONFLICT (data_type, subtype)\n" +
           "  DO UPDATE SET updated = :updated", nativeQuery = true)
    void upsertDataUpdated(final DataType dataType, final String subtype, final Instant updated);

    @Query(value =
       "select max(greatest(added_timestamp, removed_timestamp))\n" +
       "from counting_site_domain", nativeQuery = true)
    Instant getCountingSiteDomainLastUpdated();

    @Query(value =
       "select max(data_timestamp)\n" +
       "from counting_site_data", nativeQuery = true)
    Instant getCountingSiteDataLastUpdated();

    @Query(value =
           "select si.id, si.source" +
           "     , si.update_interval as updateInterval" +
           "     , si.recommended_fetch_interval as recommendedFetchInterval\n" +
           "from data_source_info si\n" +
           "WHERE id = :#{#dataSource.name()}\n" +
           "order by id", nativeQuery = true)
    DataSourceInfoDtoV1 getDataSourceInfo(final DataSource dataSource);

    default String getDataSourceUpdateInterval(final DataSource dataSource) {
        return Optional.ofNullable(getDataSourceInfo(dataSource))
            .flatMap(dataSourceInfoDtoV1 -> Optional.ofNullable(dataSourceInfoDtoV1 != null ?
                                                                dataSourceInfoDtoV1.getUpdateInterval() :
                                                                null))
            .orElse(null);
    }

    default String getDataSourceRecommendedFetchInterval(final DataSource dataSource) {
        return Optional.ofNullable(getDataSourceInfo(dataSource))
            .flatMap(dataSourceInfoDtoV1 -> Optional.ofNullable(dataSourceInfoDtoV1 != null ?
                                                                dataSourceInfoDtoV1.getRecommendedFetchInterval() :
                                                                null))
            .orElse(null);
    }
}
