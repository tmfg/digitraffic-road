package fi.livi.digitraffic.tie.dao.v1;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
}
