package fi.livi.digitraffic.tie.dao.v2;

import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.v2.maintenance.WorkMachineRealizationData;

@Repository
public interface V2WorkMachineRealizationDataRepository extends JpaRepository<WorkMachineRealizationData, Long> {

    @Query(value =  "SELECT r.*\n" +
                    "FROM WORK_MACHINE_REALIZATION_DATA r\n" +
                    "WHERE r.status = 'UNHANDLED'\n" +
                    "ORDER BY r.id ASC\n" +
                    "LIMIT :maxSize", nativeQuery = true)
    Stream<WorkMachineRealizationData> findUnhandled(int maxSize);
}
