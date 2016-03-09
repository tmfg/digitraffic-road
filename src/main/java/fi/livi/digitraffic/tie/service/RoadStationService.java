package fi.livi.digitraffic.tie.service;

import fi.livi.digitraffic.tie.model.RoadStation;
import fi.livi.digitraffic.tie.wsdl.lam.LamAsema;

public interface RoadStationService {

    RoadStation createRoadStation(final LamAsema lamAsema);

    RoadStation save(RoadStation roadStation);
}
