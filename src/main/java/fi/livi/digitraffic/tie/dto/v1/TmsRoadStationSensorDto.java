package fi.livi.digitraffic.tie.dto.v1;

import java.util.List;

import fi.livi.digitraffic.tie.model.roadstation.SensorValueDescription;
import fi.livi.digitraffic.tie.model.roadstation.VehicleClass;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Schema(description = "TMS road station sensor")
public class TmsRoadStationSensorDto extends RoadStationSensorDto {

    @Schema(description = "Vehicle class")
    @Enumerated(EnumType.STRING)
    private final VehicleClass vehicleClass;

    @Schema(description = "Lane of the sensor, 1st, 2nd, 3rd, etc.")
    private final Integer lane;

    @Schema(description = "Preset direction " +
        "(0 = Unknown direction. " +
        "1 = According to the road register address increasing direction. I.e. on the road 4 to Rovaniemi." +
        "2 = According to the road register address decreasing direction. I.e. on the road 4 to Helsinki.")
    private final Integer direction;

    public TmsRoadStationSensorDto(long naturalId, String name, String unit, String descriptionFi, String descriptionSv, String descriptionEn,
                                   String nameFi, String shortNameFi, Integer accuracy,
                                   List<SensorValueDescription> sensorValueDescriptions,
                                   String presentationNameFi, String presentationNameSv, String presentationNameEn,
                                   VehicleClass vehicleClass, Integer lane, Integer direction) {
        super(naturalId, name, unit, descriptionFi, descriptionSv, descriptionEn, nameFi, shortNameFi, accuracy, sensorValueDescriptions,
              presentationNameFi, presentationNameSv, presentationNameEn);
        this.vehicleClass = vehicleClass;
        this.lane = lane;
        this.direction = direction;
    }

    public VehicleClass getVehicleClass() {
        return vehicleClass;
    }

    public Integer getLane() {
        return lane;
    }

    public Integer getDirection() {
        return direction;
    }

}
