package fi.livi.digitraffic.tie.service;

import java.util.List;

import fi.livi.digitraffic.tie.model.RoadStation;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.wsdl.lam.LamAsema;

public interface RoadStationService {

    RoadStation createRoadStation(final LamAsema lamAsema);

    RoadStation save(RoadStation roadStation);

    List<RoadStation> findByType(RoadStationType type);
}
