package fi.livi.digitraffic.tie.metadata.service.roadstationsensor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dto.RoadStationSensorValueDto;
import fi.livi.digitraffic.tie.metadata.dao.RoadStationSensorRepository;
import fi.livi.digitraffic.tie.metadata.dao.RoadStationSensorValueDtoRepository;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;

@Service
public class RoadStationSensorServiceImpl implements RoadStationSensorService {

    private final RoadStationSensorValueDtoRepository roadStationSensorValueDtoRepository;
    private RoadStationSensorRepository roadStationSensorRepository;

    private final int roadWeatherStationSensorValueTimeLimitInMins;
    private final ArrayList<Long> includedSensorNaturalIds;

    @Autowired
    public RoadStationSensorServiceImpl(final RoadStationSensorValueDtoRepository roadStationSensorValueDtoRepository,
                                        final RoadStationSensorRepository roadStationSensorRepository,
                                        @Value("${roadWeatherStation.sensorValue.timeLimit}")
                                        final int roadWeatherStationSensorValueTimeLimitInMins,
                                        @Value("${roadWeatherStation.includedSensorNaturalIds}")
                                        final String includedSensorNaturalIdsStr) {
        this.roadStationSensorValueDtoRepository = roadStationSensorValueDtoRepository;
        this.roadStationSensorRepository = roadStationSensorRepository;
        this.roadWeatherStationSensorValueTimeLimitInMins = roadWeatherStationSensorValueTimeLimitInMins;
        String[] ids = StringUtils.splitPreserveAllTokens(includedSensorNaturalIdsStr, ',');
        includedSensorNaturalIds = new ArrayList<>();
        for (String id : ids) {
            includedSensorNaturalIds.add(Long.parseLong(id.trim()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoadStationSensor> findAllNonObsoleteRoadStationSensors() {
        return roadStationSensorRepository.findNonObsoleteRoadStationSensors();
    }

    @Transactional(readOnly = true)
    @Override
    public Map<Long, List<RoadStationSensorValueDto>> findAllNonObsoleteRoadWeatherStationSensorValues() {
        Map<Long, List<RoadStationSensorValueDto>> rsNaturalIdToRsSensorValues = new HashMap<>();
        List<RoadStationSensorValueDto> sensors =
                roadStationSensorValueDtoRepository.findAllNonObsoleteRoadStationSensorValues(
                        RoadStationType.WEATHER_STATION.getTypeNumber(),
                        roadWeatherStationSensorValueTimeLimitInMins,
                        includedSensorNaturalIds);
        for (RoadStationSensorValueDto sensor : sensors) {
            List<RoadStationSensorValueDto> values = rsNaturalIdToRsSensorValues.get(Long.valueOf(sensor.getRoadStationNaturalId()));
            if (values == null) {
                values = new ArrayList<>();
                rsNaturalIdToRsSensorValues.put(sensor.getRoadStationNaturalId(), values);
            }

            values.add(sensor);
        }
        return rsNaturalIdToRsSensorValues;
    }
}
