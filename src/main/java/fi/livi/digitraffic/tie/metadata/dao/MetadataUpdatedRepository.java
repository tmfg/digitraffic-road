package fi.livi.digitraffic.tie.metadata.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.DataUpdated;

@Repository
public interface MetadataUpdatedRepository extends JpaRepository<DataUpdated, Long> {

    @Query(value =
           "SELECT *\n" +
           "FROM METADATA_UPDATED\n" +
           "WHERE METADATA_TYPE = :metadataType",
           nativeQuery = true)
    DataUpdated findByMetadataType(@Param("metadataType")
                                   final String metadataType);
}
