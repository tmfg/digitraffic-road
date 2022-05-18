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

    String UNSET_VERSION = "-";

    @Query("SELECT max(d.updatedTime)\n" +
           "FROM DataUpdated d\n" +
           "WHERE d.dataType in (:dataTypes)\n" +
           " AND d.version = '" + UNSET_VERSION + "'")
    Instant findUpdatedTime(@Param("dataTypes") final DataType...dataTypes);

    @Query("SELECT max(d.updatedTime)\n" +
           "FROM DataUpdated d\n" +
           "WHERE d.dataType = :dataType" +
           "  AND d.version in (:versions)")
    Instant findUpdatedTime(@Param("dataType") final DataType dataType, @Param("versions") final List<String> versions);

    @Query(value = "select transaction_timestamp()", nativeQuery = true)
    Instant getTransactionStartTime();

    @Modifying
    default void upsertDataUpdated(final DataType dataType) {
        upsertDataUpdated(dataType, (String)null);
    }

    @Modifying
    default void upsertDataUpdated(final DataType dataType, final Instant updated) {
        upsertDataUpdated(dataType, null, updated);
    }

    @Modifying
    @Query(value =
           "INSERT INTO data_updated(id, data_type, version, updated)\n" +
           "  VALUES(NEXTVAL('seq_data_updated'), :#{#dataType.name()}, coalesce(cast(:version AS TEXT), '" + UNSET_VERSION + "'), now())\n" +
           "  ON CONFLICT (data_type, version)\n" +
           "  DO UPDATE SET updated = now()", nativeQuery = true)
    void upsertDataUpdated(final DataType dataType, final String version);


    @Modifying
    @Query(value =
           "INSERT INTO data_updated(id, data_type, version, updated)\n" +
           "  VALUES(NEXTVAL('seq_data_updated'), :#{#dataType.name()}, coalesce(cast(:version AS TEXT), '" + UNSET_VERSION + "'), :updated)\n" +
           "  ON CONFLICT (data_type, version)\n" +
           "  DO UPDATE SET updated = :updated", nativeQuery = true)
    void upsertDataUpdated(final DataType dataType, final String version, final Instant updated);
}
