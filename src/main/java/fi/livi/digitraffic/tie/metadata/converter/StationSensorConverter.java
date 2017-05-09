package fi.livi.digitraffic.tie.metadata.converter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.metadata.dao.RoadStationSensorRepository;
import fi.livi.digitraffic.tie.metadata.dto.StationSensor;

@Component
public class StationSensorConverter {
    private final RoadStationSensorRepository roadStationSensorRepository;

    @Autowired
    public StationSensorConverter(final RoadStationSensorRepository roadStationSensorRepository) {
        this.roadStationSensorRepository = roadStationSensorRepository;
    }

    public Map<Long, List<Long>> createSensorMap(final String type) {
        final List<StationSensor> list = roadStationSensorRepository.listStationSensorsByType(type);
        final Map<Long, List<Long>> sensorMap = new HashMap<>();

        list.stream().forEach(ss -> sensorMap.put(ss.getRoadStationId(), sensorList(ss.getSensors())));

        return sensorMap;
    }

    public Map<Long, List<Long>> createSensorMap(final Long roadStationId, final String type) {
        final Map<Long, List<Long>> sensorMap = new HashMap<>();

        if(roadStationId != null) {
            final StationSensor stationSensor = roadStationSensorRepository.listRoadStationSensorsByIdAndType(roadStationId, type);

            sensorMap.put(stationSensor.getRoadStationId(), sensorList(stationSensor.getSensors()));
        }

        return sensorMap;
    }

    private static List<Long> sensorList(final String sensorList) {
        return Stream.of(sensorList.split(",")).map(Long::valueOf).collect(Collectors.toList());
    }

}
