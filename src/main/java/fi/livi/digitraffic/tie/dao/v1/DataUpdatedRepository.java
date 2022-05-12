package fi.livi.digitraffic.tie.dao.v1;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v1.DataUpdated;

@Repository
public interface DataUpdatedRepository extends JpaRepository<DataUpdated, Long> {

    DataUpdated findByDataType(final DataType dataType);

    DataUpdated findByDataTypeAndVersion(final DataType dataType, final String version);

    @Query("SELECT max(d.updatedTime)\n" +
           "FROM DataUpdated d\n" +
           "WHERE d.dataType in (:dataTypes)")
    Instant findUpdatedTime(@Param("dataTypes") final DataType...dataTypes);

    @Query("SELECT max(d.updatedTime)\n" +
           "FROM DataUpdated d\n" +
           "WHERE d.dataType = :dataType" +
           "  AND d.version in (:versions)")
    Instant findUpdatedTime(@Param("dataType") final DataType dataType, @Param("versions") final List<String> versions);

    @Query(value = "select transaction_timestamp()", nativeQuery = true)
    Instant getTransactionStartTime();

}
