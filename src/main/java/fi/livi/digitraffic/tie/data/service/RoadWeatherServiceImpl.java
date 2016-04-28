package fi.livi.digitraffic.tie.data.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.data.model.RoadWeatherDataObject;
import fi.livi.digitraffic.tie.data.model.dto.RoadStationSensorValueDto;
import fi.livi.digitraffic.tie.data.model.dto.RoadWeatherStationDto;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;

@Service
public class RoadWeatherServiceImpl implements RoadWeatherService {

    private RoadStationSensorService roadStationSensorService;
    private RoadStationService roadStationService;

    @Autowired
    public RoadWeatherServiceImpl(final RoadStationSensorService roadStationSensorService,
                                  final RoadStationService roadStationService) {

        this.roadStationSensorService = roadStationSensorService;
        this.roadStationService = roadStationService;
    }

    @Override
    public RoadWeatherDataObject findAllWeatherData() {

        Map<Long, List<RoadStationSensorValueDto>> values = roadStationSensorService.findAllNonObsoleteRoadWeatherStationSensorValues();
        List<RoadWeatherStationDto> stations = new ArrayList<>();
        for (Map.Entry<Long, List<RoadStationSensorValueDto>> entry : values.entrySet()) {
            RoadWeatherStationDto dto = new RoadWeatherStationDto();
            stations.add(dto);
            dto.setRoadStationNaturalId(entry.getKey());
            dto.setSensorValues(entry.getValue());
        }

        return new RoadWeatherDataObject(stations);
    }
}
