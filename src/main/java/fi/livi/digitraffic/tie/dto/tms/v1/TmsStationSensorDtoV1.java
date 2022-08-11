package fi.livi.digitraffic.tie.dto.tms.v1;

import java.util.List;

import fi.livi.digitraffic.tie.dto.roadstation.v1.RoadStationSensorDirection;
import fi.livi.digitraffic.tie.dto.roadstation.v1.RoadStationSensorDtoV1;
import fi.livi.digitraffic.tie.model.v1.SensorValueDescription;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "TMS road station sensor")
public class TmsStationSensorDtoV1 extends RoadStationSensorDtoV1 {

    @Schema(description = RoadStationSensorDirection.API_DESCRIPTION)
    public final RoadStationSensorDirection direction;

    public TmsStationSensorDtoV1(long naturalId, String nameFi, String shortNameFi, String unit, Integer accuracy,
                                 String descriptionFi, String descriptionSv, String descriptionEn,

                                 List<SensorValueDescription> sensorValueDescriptions,
                                 String presentationNameFi, String presentationNameSv, String presentationNameEn,
                                 Integer direction) {
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
