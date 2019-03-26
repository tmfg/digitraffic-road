package fi.livi.digitraffic.tie.metadata.dao;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.model.DataUpdated;

@Repository
public interface DataUpdatedRepository extends JpaRepository<DataUpdated, Long> {

    @Query(value =
           "SELECT d\n" +
           "FROM DataUpdated d\n" +
           "WHERE d.dataType = :dataType")
    DataUpdated findByDataType(@Param("dataType") final DataType dataType);

    @Query("SELECT d.updatedTime\n" +
           "FROM DataUpdated d\n" +
           "WHERE d.dataType = :dataType")
    Instant findUpdatedTime(@Param("dataType") final DataType dataType);

    @Query(value = "select transaction_timestamp()", nativeQuery = true)
    Instant getTransactionStartTime();

}
