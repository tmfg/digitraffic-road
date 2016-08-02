package fi.livi.digitraffic.tie.metadata.service.roadweather;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.converter.RoadWeatherStationMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.metadata.dao.RoadStationSensorRepository;
import fi.livi.digitraffic.tie.metadata.dao.RoadWeatherStationRepository;
import fi.livi.digitraffic.tie.metadata.dao.SensorValueRepository;
import fi.livi.digitraffic.tie.metadata.geojson.roadweather.RoadWeatherStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadWeatherStation;
import fi.livi.digitraffic.tie.metadata.model.SensorValue;

@Service
public class RoadWeatherStationServiceImpl implements RoadWeatherStationService {

    private final RoadWeatherStationRepository roadWeatherStationRepository;
    private final RoadStationSensorRepository roadStationSensorRepository;
    private final SensorValueRepository sensorValueRepository;

    @Autowired
    public RoadWeatherStationServiceImpl(final RoadWeatherStationRepository roadWeatherStationRepository,
                                         final RoadStationSensorRepository roadStationSensorRepository,
                                         final SensorValueRepository sensorValueRepository) {

        this.roadWeatherStationRepository = roadWeatherStationRepository;
        this.roadStationSensorRepository = roadStationSensorRepository;
        this.sensorValueRepository = sensorValueRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public Map<Long, RoadWeatherStation> findAllRoadWeatherStationsMappedByLotjuId() {
        final Map<Long, RoadWeatherStation> map = new HashMap<>();
        final List<RoadWeatherStation> all = roadWeatherStationRepository.findAll();
        for (final RoadWeatherStation roadWeatherStation : all) {
            map.put(roadWeatherStation.getLotjuId(), roadWeatherStation);
        }
        return map;
    }

    @Transactional
    @Override
    public RoadWeatherStation save(final RoadWeatherStation roadWeatherStation) {
        final RoadWeatherStation rws = roadWeatherStationRepository.save(roadWeatherStation);
        roadWeatherStationRepository.flush();
        return rws;
    }

    @Transactional(readOnly = true)
    @Override
    public List<RoadStationSensor> findAllRoadStationSensors() {
        return roadStationSensorRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Map<Long, RoadStationSensor> findAllRoadStationSensorsMappedByNaturalId() {
        final List<RoadStationSensor> all = findAllRoadStationSensors();

        final HashMap<Long, RoadStationSensor> naturalIdToRSS = new HashMap<>();
        for (final RoadStationSensor roadStationSensor : all) {
            if ( !roadStationSensor.isStatusSensor() ) {
                naturalIdToRSS.put(roadStationSensor.getNaturalId(), roadStationSensor);
            }
        }
        return naturalIdToRSS;
    }

    @Transactional
    @Override
    public RoadStationSensor saveRoadStationSensor(final RoadStationSensor roadStationSensor) {
        final RoadStationSensor sensor = roadStationSensorRepository.save(roadStationSensor);
        roadStationSensorRepository.flush();
        return sensor;
    }

    @Transactional(readOnly = true)
    @Override
    public List<SensorValue> findAllSensorValues() {
        return sensorValueRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public RoadWeatherStationFeatureCollection findAllNonObsoletePublicRoadWeatherStationAsFeatureCollection() {
        return RoadWeatherStationMetadata2FeatureConverter.convert(
                roadWeatherStationRepository.findByRoadStationObsoleteFalseAndRoadStationIsPublicTrueOrderByRoadStation_NaturalId());
    }
}
