package fi.livi.digitraffic.tie.data.service;

import java.util.List;

import fi.livi.digitraffic.tie.data.model.trafficfluency.FluencyClass;
import fi.livi.digitraffic.tie.data.model.trafficfluency.TrafficFluencyDataObject;

public interface TrafficFluencyService {

    TrafficFluencyDataObject listCurrentTrafficFluencyData();

    List<FluencyClass> findAllFluencyClassesOrderByLowerLimitAsc();
}
