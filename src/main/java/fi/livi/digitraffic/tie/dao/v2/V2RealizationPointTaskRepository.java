package fi.livi.digitraffic.tie.dao.v2;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.v2.maintenance.V2RealizationPointTask;
import fi.livi.digitraffic.tie.model.v2.maintenance.V2RealizationPointTaskPK;
import fi.livi.digitraffic.tie.model.v2.maintenance.V2RealizationTask;

@Repository
public interface V2RealizationPointTaskRepository extends JpaRepository<V2RealizationPointTask, V2RealizationPointTaskPK> {

}
