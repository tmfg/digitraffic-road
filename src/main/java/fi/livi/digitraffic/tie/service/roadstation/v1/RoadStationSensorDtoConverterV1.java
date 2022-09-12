package fi.livi.digitraffic.tie.service.roadstation.v1;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fi.livi.digitraffic.tie.dto.tms.v1.TmsStationSensorDtoV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationSensorDtoV1;
import fi.livi.digitraffic.tie.model.v1.RoadStationSensor;

public class RoadStationSensorDtoConverterV1 {

    public static List<WeatherStationSensorDtoV1> convertWeatherSensors(final List<RoadStationSensor> sensors) {
        return sensors.stream()
            .map(s -> new WeatherStationSensorDtoV1(
                s.getNaturalId(), s.getNameFi(), s.getShortNameFi(), s.getUnit(), s.getAccuracy(),
                s.getDescriptionFi(), s.getDescriptionSv(), s.getDescriptionEn(),
                new ArrayList<>(s.getSensorValueDescriptions()), s.getPresentationNameFi(),
                s.getPresentationNameSv(), s.getPresentationNameEn(), s.getDirection()))
            .collect(Collectors.toList());
    }

    public static List<TmsStationSensorDtoV1> convertTmsSensors(final List<RoadStationSensor> sensors) {
        return sensors.stream()
            .map(s -> new TmsStationSensorDtoV1(
                s.getNaturalId(), s.getNameFi(), s.getShortNameFi(), s.getUnit(), s.getAccuracy(),
                s.getDescriptionFi(), s.getDescriptionSv(), s.getDescriptionEn(),
                  new ArrayList<>(s.getSensorValueDescriptions()), s.getPresentationNameFi(),
                s.getPresentationNameSv(), s.getPresentationNameEn(), s.getDirection()))
            .collect(Collectors.toList());
    }
}
