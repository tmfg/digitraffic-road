package fi.livi.digitraffic.tie.metadata.service.roadstation;

import java.util.List;

import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;

public interface RoadStationService {

    RoadStation save(RoadStation roadStation);

    List<RoadStation> findByType(RoadStationType type);

    RoadStation findByTypeAndNaturalId(RoadStationType type, long naturalId);

    List<RoadStation> findOrphanWeatherStationRoadStations();

    List<RoadStation> findOrphanCameraStationRoadStations();
}
