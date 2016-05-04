package fi.livi.digitraffic.tie.data.service;

import java.util.List;

import fi.livi.digitraffic.tie.data.model.trafficfluency.FluencyClass;
import fi.livi.digitraffic.tie.data.dto.trafficfluency.TrafficFluencyDataObjectDto;

public interface TrafficFluencyService {

    TrafficFluencyDataObjectDto listCurrentTrafficFluencyData();

    List<FluencyClass> findAllFluencyClassesOrderByLowerLimitAsc();
}
