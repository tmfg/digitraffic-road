package fi.livi.digitraffic.tie.dao.v2;

import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.v2.maintenance.V2RealizationData;

@Repository
public interface V2RealizationDataRepository extends JpaRepository<V2RealizationData, Long> {

    @Query(value =  "SELECT r.*\n" +
                    "FROM V2_REALIZATION_DATA r\n" +
                    "WHERE r.status = 'UNHANDLED'\n" +
                    "ORDER BY r.id\n" +
                    "LIMIT :maxSize", nativeQuery = true)
    Stream<V2RealizationData> findUnhandled(final int maxSize);
}
