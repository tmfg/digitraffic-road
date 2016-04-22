package fi.livi.digitraffic.tie.metadata.service.roadweather;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.metadata.converter.RoadWeatherStationMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.metadata.dao.RoadWeatherSensorRepository;
import fi.livi.digitraffic.tie.metadata.dao.RoadWeatherStationRepository;
import fi.livi.digitraffic.tie.metadata.geojson.roadweather.RoadWeatherStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.RoadWeatherSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadWeatherStation;

@Service
public class RoadWeatherStationServiceImpl implements RoadWeatherStationService{

    private final RoadWeatherStationRepository roadWeatherStationRepository;
    private RoadWeatherSensorRepository roadWeatherSensorRepository;

    @Autowired
    public RoadWeatherStationServiceImpl(final RoadWeatherStationRepository roadWeatherStationRepository,
                                         final RoadWeatherSensorRepository roadWeatherSensorRepository) {

        this.roadWeatherStationRepository = roadWeatherStationRepository;
        this.roadWeatherSensorRepository = roadWeatherSensorRepository;
    }

    @Override
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

    @Override
    public List<RoadWeatherSensor> findAllRoadStationSensors() {
        return roadWeatherSensorRepository.findAll();
    }

    @Override
    public Map<Long, List<RoadWeatherSensor>> findAllRoadStationSensorsMappedByRoadStationLotjuId() {
        List<RoadWeatherSensor> all = findAllRoadStationSensors();
        final Map<Long, List<RoadWeatherSensor>> map = new HashMap<>();
        for (RoadWeatherSensor rws : all) {
            List<RoadWeatherSensor> list = map.get(rws.getRoadWeatherStationLotjuId());
            if (list == null) {
                list = new ArrayList<>();
                map.put(rws.getRoadWeatherStationLotjuId(), list);
            }
            list.add(rws);
        }
        return map;
    }

    @Override
    public RoadWeatherSensor save(RoadWeatherSensor roadWeatherSensor) {
        final RoadWeatherSensor rws = roadWeatherSensorRepository.save(roadWeatherSensor);
        roadWeatherSensorRepository.flush();
        return rws;
    }

    @Override
    public RoadWeatherStationFeatureCollection findAllNonObsoleteRoadWeatherStationAsFeatureCollection() {
        return RoadWeatherStationMetadata2FeatureConverter.convert(roadWeatherStationRepository.findByRoadStationObsoleteFalse());
    }
}
