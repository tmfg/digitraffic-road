package fi.livi.digitraffic.tie.service.roadweather;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.livi.digitraffic.tie.dao.RoadWeatherStationRepository;
import fi.livi.digitraffic.tie.model.RoadWeatherStation;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoadWeatherStationServiceImpl implements RoadWeatherStationService{

    private static final Logger LOG = Logger.getLogger(RoadWeatherStationServiceImpl.class);

    private RoadWeatherStationRepository roadWeatherStationRepository;

    @Autowired
    public RoadWeatherStationServiceImpl(RoadWeatherStationRepository roadWeatherStationRepository) {

        this.roadWeatherStationRepository = roadWeatherStationRepository;
    }

    public Map<Long, RoadWeatherStation> findAllRoadWeatherStationsMappedByLotjuId() {
        Map<Long, RoadWeatherStation> map = new HashMap<>();
        List<RoadWeatherStation> all = roadWeatherStationRepository.findAll();
        for (RoadWeatherStation roadWeatherStation : all) {
            map.put(roadWeatherStation.getLotjuId(), roadWeatherStation);
        }
        return map;
    }

    @Override
    public RoadWeatherStation save(RoadWeatherStation roadWeatherStation) {
        RoadWeatherStation rws = roadWeatherStationRepository.save(roadWeatherStation);
        roadWeatherStationRepository.flush();
        return rws;
    }
}
