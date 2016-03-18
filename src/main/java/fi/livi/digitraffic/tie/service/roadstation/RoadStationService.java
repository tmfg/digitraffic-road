package fi.livi.digitraffic.tie.service.roadstation;

import java.util.List;

import fi.livi.digitraffic.tie.model.RoadStation;
import fi.livi.digitraffic.tie.model.RoadStationType;

public interface RoadStationService {

    RoadStation save(RoadStation roadStation);

    List<RoadStation> findByType(RoadStationType type);

    List<RoadStation> findOrphansByType(RoadStationType type);
}
