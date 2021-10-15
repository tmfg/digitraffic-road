package fi.livi.digitraffic.tie.converter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.v1.RoadStationSensorRepository;
import fi.livi.digitraffic.tie.dto.v1.StationSensors;
import fi.livi.digitraffic.tie.model.RoadStationType;

@Service
public class StationSensorConverterService {
    private final RoadStationSensorRepository roadStationSensorRepository;

    @Autowired
    public StationSensorConverterService(final RoadStationSensorRepository roadStationSensorRepository) {
        this.roadStationSensorRepository = roadStationSensorRepository;
    }

    @Transactional(readOnly = true)
    public Map<Long, List<Long>> getPublishableSensorMap(final RoadStationType type) {
        if (type.equals(RoadStationType.TMS_STATION) || type.equals(RoadStationType.WEATHER_STATION)) {
            final List<StationSensors> list = roadStationSensorRepository.listStationPublishableSensorsByType(type.name());
            return createMap(list);
        }
        throw new IllegalArgumentException(String.format("RoadStationType %s not suported", type.name()));
    }

    @Transactional(readOnly = true)
    public Map<Long, List<Long>> getPublishableSensorMap(final Long roadStationId, final RoadStationType type) {
        if (type.equals(RoadStationType.TMS_STATION) || type.equals(RoadStationType.WEATHER_STATION)) {
        return roadStationId == null ?
            Collections.emptyMap() :
            createMap(roadStationSensorRepository.getStationPublishableSensorsByStationIdAndType(roadStationId, type.name()));
        }
        throw new IllegalArgumentException(String.format("RoadStationType %s not suported", type.name()));
    }

    private static Map<Long, List<Long>> createMap(final List<StationSensors> sensors) {
        final Map<Long, List<Long>> sensorMap = new HashMap<>();

        sensors.forEach(ss -> sensorMap.put(ss.getRoadStationId(), sensorList(ss.getSensors())));

        return sensorMap;
    }

    private static List<Long> sensorList(final String sensorList) {
        return Stream.of(sensorList.split(",")).map(Long::valueOf).collect(Collectors.toList());
    }

}
