package fi.livi.digitraffic.tie.metadata.service.roadweather;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.livi.digitraffic.tie.metadata.dao.RoadWeatherStationRepository;
import fi.livi.digitraffic.tie.metadata.model.RoadWeatherStation;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoadWeatherStationServiceImpl implements RoadWeatherStationService{

    private static final Logger LOG = Logger.getLogger(RoadWeatherStationServiceImpl.class);

    private final RoadWeatherStationRepository roadWeatherStationRepository;

    @Autowired
    public RoadWeatherStationServiceImpl(final RoadWeatherStationRepository roadWeatherStationRepository) {

        this.roadWeatherStationRepository = roadWeatherStationRepository;
    }

    public Map<Long, RoadWeatherStation> findAllRoadWeatherStationsMappedByLotjuId() {
        final Map<Long, RoadWeatherStation> map = new HashMap<>();
        final List<RoadWeatherStation> all = roadWeatherStationRepository.findAll();
        for (final RoadWeatherStation roadWeatherStation : all) {
            map.put(roadWeatherStation.getLotjuId(), roadWeatherStation);
        }
        return map;
    }

    @Override
    public RoadWeatherStation save(final RoadWeatherStation roadWeatherStation) {
        final RoadWeatherStation rws = roadWeatherStationRepository.save(roadWeatherStation);
        roadWeatherStationRepository.flush();
        return rws;
    }
}
