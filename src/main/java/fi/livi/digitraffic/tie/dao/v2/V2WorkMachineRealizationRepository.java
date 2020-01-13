package fi.livi.digitraffic.tie.dao.v2;

import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.v2.maintenance.WorkMachineRealization;

@Repository
public interface V2WorkMachineRealizationRepository extends JpaRepository<WorkMachineRealization, Long> {

    @Query(value =  "SELECT r.*\n" +
                    "FROM WORK_MACHINE_REALIZATION r\n" +
                    "WHERE r.status = 'UNHANDLED'\n" +
                    "ORDER BY r.id ASC\n" +
                    "LIMIT :maxSize", nativeQuery = true)
    Stream<WorkMachineRealization> findUnhandled(int maxSize);
}
