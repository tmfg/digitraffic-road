package fi.livi.digitraffic.tie.dto.v1;

import java.util.List;

import fi.livi.digitraffic.tie.model.roadstation.SensorValueDescription;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Weather road station sensor", name = "WeatherRoadStationSensor")
public class WeatherRoadStationSensorDto extends RoadStationSensorDto {
    public WeatherRoadStationSensorDto(long naturalId, String name, String unit, String descriptionFi, String descriptionSv, String descriptionEn,
                                       String nameFi, String shortNameFi, Integer accuracy,
                                       List<SensorValueDescription> sensorValueDescriptions,
                                       String presentationNameFi, String presentationNameSv, String presentationNameEn) {
        super(naturalId, name, unit, descriptionFi, descriptionSv, descriptionEn, nameFi, shortNameFi, accuracy, sensorValueDescriptions,
              presentationNameFi, presentationNameSv, presentationNameEn);
    }
}
