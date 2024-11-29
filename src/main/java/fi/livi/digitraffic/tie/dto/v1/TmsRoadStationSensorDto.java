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

    @Schema(description = "Measurement direction " +
        "1 = According to the road register address increasing direction. I.e. on the road 4 to Rovaniemi." +
        "2 = According to the road register address decreasing direction. I.e. on the road 4 to Helsinki.")
    private final Integer direction;

    public TmsRoadStationSensorDto(final long naturalId, final String name, final String unit, final String descriptionFi, final String descriptionSv, final String descriptionEn,
                                   final String nameFi, final String shortNameFi, final Integer accuracy,
                                   final List<SensorValueDescription> sensorValueDescriptions,
                                   final String presentationNameFi, final String presentationNameSv, final String presentationNameEn,
                                   final VehicleClass vehicleClass, final Integer lane, final Integer direction) {
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
