package fi.livi.digitraffic.tie.dto.weather.v1;

import java.util.List;

import fi.livi.digitraffic.tie.dto.roadstation.v1.RoadStationSensorDirection;
import fi.livi.digitraffic.tie.dto.roadstation.v1.RoadStationSensorDtoV1;
import fi.livi.digitraffic.tie.model.v1.SensorValueDescription;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Weather road station sensor")
public class WeatherStationSensorDtoV1 extends RoadStationSensorDtoV1 {

    @Schema(description = RoadStationSensorDirection.API_DESCRIPTION)
    public final RoadStationSensorDirection direction;

    public WeatherStationSensorDtoV1(final long naturalId,
                                     final String nameFi,
                                     final String shortNameFi,
                                     final String unit,
                                     final Integer accuracy,
                                     final String descriptionFi,
                                     final String descriptionSv,
                                     final String descriptionEn,
                                     final List<SensorValueDescription> sensorValueDescriptions,
                                     final String presentationNameFi,
                                     final String presentationNameSv,
                                     final String presentationNameEn,
                                     final Integer direction) {
        super(naturalId, nameFi, shortNameFi, unit, accuracy,
              descriptionFi, descriptionSv, descriptionEn,
              presentationNameFi, presentationNameSv, presentationNameEn,
              sensorValueDescriptions);
        if (direction != null) {
            this.direction = RoadStationSensorDirection.fromValue(direction);
        } else {
            this.direction = RoadStationSensorDirection.fromSensorName(nameFi);
        }
    }

}
