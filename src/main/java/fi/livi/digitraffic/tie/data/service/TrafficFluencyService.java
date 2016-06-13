package fi.livi.digitraffic.tie.data.service;

import java.util.List;

import fi.livi.digitraffic.tie.data.dto.trafficfluency.TrafficFluencyRootDataObjectDto;
import fi.livi.digitraffic.tie.data.model.FluencyClass;

public interface TrafficFluencyService {

    TrafficFluencyRootDataObjectDto listCurrentTrafficFluencyData();

    List<FluencyClass> findAllFluencyClassesOrderByLowerLimitAsc();
}
