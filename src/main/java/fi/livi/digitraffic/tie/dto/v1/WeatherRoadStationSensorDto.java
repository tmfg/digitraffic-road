package fi.livi.digitraffic.tie.dto.v1;

import java.util.List;

import fi.livi.digitraffic.tie.model.roadstation.SensorValueDescription;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Weather road station sensor", name = "WeatherRoadStationSensor")
public class WeatherRoadStationSensorDto extends RoadStationSensorDto {
    public WeatherRoadStationSensorDto(final long naturalId, final String name, final String unit, final String descriptionFi, final String descriptionSv, final String descriptionEn,
                                       final String nameFi, final String shortNameFi, final Integer accuracy,
                                       final List<SensorValueDescription> sensorValueDescriptions,
                                       final String presentationNameFi, final String presentationNameSv, final String presentationNameEn) {
        super(naturalId, name, unit, descriptionFi, descriptionSv, descriptionEn, nameFi, shortNameFi, accuracy, sensorValueDescriptions,
              presentationNameFi, presentationNameSv, presentationNameEn);
    }
}
