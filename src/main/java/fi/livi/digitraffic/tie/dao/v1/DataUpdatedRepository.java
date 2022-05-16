package fi.livi.digitraffic.tie.dao.v1;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v1.DataUpdated;

@Repository
public interface DataUpdatedRepository extends JpaRepository<DataUpdated, Long> {

    String UNSET_EXTENSION = "-";

    @Query("SELECT max(d.updatedTime)\n" +
           "FROM DataUpdated d\n" +
           "WHERE d.dataType in (:dataTypes)\n" +
           " AND d.extension = '" + UNSET_EXTENSION + "'")
    Instant findUpdatedTime(@Param("dataTypes") final DataType...dataTypes);

    @Query("SELECT max(d.updatedTime)\n" +
           "FROM DataUpdated d\n" +
           "WHERE d.dataType = :dataType" +
           "  AND d.extension in (:extensions)")
    Instant findUpdatedTime(@Param("dataType") final DataType dataType, @Param("extensions") final List<String> extensions);

    @Query(value = "select transaction_timestamp()", nativeQuery = true)
    Instant getTransactionStartTime();

    @Modifying
    default void upsertDataUpdated(final DataType dataType) {
        upsertDataUpdated(dataType, null);
    }

    @Modifying
    @Query(value =
           "INSERT INTO data_updated(id, data_type, extension, updated)\n" +
           "  VALUES(NEXTVAL('seq_data_updated'), :#{#dataType.name()}, coalesce(cast(:extension AS TEXT), '" + UNSET_EXTENSION + "'), now())\n" +
           "  ON CONFLICT (data_type, extension)\n" +
           "  DO UPDATE SET updated = now()", nativeQuery = true)
    void upsertDataUpdated(final DataType dataType, final String extension);


    @Modifying
    @Query(value =
           "INSERT INTO data_updated(id, data_type, extension, updated)\n" +
           "  VALUES(NEXTVAL('seq_data_updated'), :#{#dataType.name()}, :extension, :updated)\n" +
           "  ON CONFLICT (data_type, extension)\n" +
           "  DO UPDATE SET updated = :updated", nativeQuery = true)
    void upsertDataUpdated(final DataType dataType, final String extension, final Instant updated);
}
