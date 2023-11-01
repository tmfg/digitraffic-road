package fi.livi.digitraffic.tie.dto.roadstation.v1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.model.roadstation.SensorValueDescription;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Road station sensor")
@JsonPropertyOrder(value = {"id", "name", "shortName", "descriptionFi", "descriptionSv", "descriptionEn", "unit", "accuracy", "nameOld", "sensorValueDescriptions"})
public abstract class RoadStationSensorDtoV1 {

    @Schema(description = "Sensor id", required = true)
    @JsonProperty("id")
    public final long naturalId;

    @Schema(description = "Sensor name [fi]")
    @JsonProperty(value = "name")
    public final String nameFi;

    @Schema(description = "Short name for sensor [fi]")
    @JsonProperty(value = "shortName")
    public final String shortNameFi;

    @Schema(description = "Unit of sensor value")
    public final String unit;

    @Schema(description = "Sensor accuracy")
    public final Integer accuracy;

    @Schema(description = "Map of presentation names [fi, sv, en]")
    public final Map<String, String> presentationNames = new HashMap<>();

    @Schema(description = "Map of sensor descriptions [fi, sv, en]")
    public final Map<String, String> descriptions = new HashMap<>();

    @Schema(description = "Descriptions for sensor values")
    public final List<SensorValueDescription> sensorValueDescriptions;

    public RoadStationSensorDtoV1(final long naturalId, final String nameFi, final String shortNameFi, final String unit, final Integer accuracy,
                                  final String descriptionFi, final String descriptionSv, final String descriptionEn,
                                  final String presentationNameFi, final String presentationNameSv, final String presentationNameEn,
                                  final List<SensorValueDescription> sensorValueDescriptions) {
        this.naturalId = naturalId;
        this.unit = unit;
        this.nameFi = nameFi;
        this.shortNameFi = shortNameFi;
        this.accuracy = accuracy;
        this.sensorValueDescriptions = sensorValueDescriptions;
        this.presentationNames.put("fi", presentationNameFi);
        this.presentationNames.put("en", presentationNameEn);
        this.presentationNames.put("sv", presentationNameSv);
        this.descriptions.put("fi", descriptionFi);
        this.descriptions.put("en", descriptionEn);
        this.descriptions.put("sv", descriptionSv);
    }

    @Schema(description = "Sensor description [fi]")
    public String getDescription() {
        return descriptions.get("fi");
    }
}
