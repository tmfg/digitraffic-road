package fi.livi.digitraffic.tie.dto.v1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.model.roadstation.SensorValueDescription;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Road station sensor")
@JsonPropertyOrder(value = {"id", "name", "shortName", "descriptionFi", "descriptionSv", "descriptionEn", "unit", "accuracy", "nameOld", "sensorValueDescriptions"})
public abstract class RoadStationSensorDto {

    @Schema(description = "Sensor id")
    @JsonProperty("id")
    private final long naturalId;

    @Schema(description = "Sensor old name. For new sensors will equal name. Will deprecate in future.")
    @JsonProperty(value = "nameOld")
    private final String name;

    @Schema(description = "Unit of sensor value")
    private final String unit;

    @Schema(description = "Sensor name [fi]")
    @JsonProperty(value = "name")
    private final String nameFi;

    @Schema(description = "Short name for sensor [fi]")
    @JsonProperty(value = "shortName")
    private final String shortNameFi;

    @Schema(description = "Sensor accuracy")
    private final Integer accuracy;

    @Schema(description = "Possible additional descriptions for sensor values")
    private final List<SensorValueDescription> sensorValueDescriptions;

    @Schema(description = "Map of descriptions [fi, sv, en]")
    private final Map<String, String> descriptions = new HashMap<>();

    @Schema(description = "Map of presentation names [fi, sv, en]")
    private final Map<String, String> presentationNames = new HashMap<>();

    public RoadStationSensorDto(long naturalId, String name, String unit, String descriptionFi, String descriptionSv, String descriptionEn,
                                String nameFi, String shortNameFi, Integer accuracy,
                                List<SensorValueDescription> sensorValueDescriptions, String presentationNameFi, String presentationNameSv,
                                String presentationNameEn) {
        this.naturalId = naturalId;
        this.name = name;
        this.unit = unit;
        this.nameFi = nameFi;
        this.shortNameFi = shortNameFi;
        this.accuracy = accuracy;
        this.sensorValueDescriptions = sensorValueDescriptions;
        addPresentationName("fi", presentationNameFi);
        addPresentationName("en", presentationNameEn);
        addPresentationName("sv", presentationNameSv);
        addDescription("fi", descriptionFi);
        addDescription("en", descriptionEn);
        addDescription("sv", descriptionSv);

    }

    public long getNaturalId() {
        return naturalId;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    @Schema(description = "Sensor description [fi]")
    public String getDescription() {
        return descriptions.get("fi");
    }

    public String getNameFi() {
        return nameFi;
    }

    public String getShortNameFi() {
        return shortNameFi;
    }

    public Integer getAccuracy() {
        return accuracy;
    }

    public List<SensorValueDescription> getSensorValueDescriptions() {
        return sensorValueDescriptions;
    }

    public void addPresentationName(final String lang, final String name) {
        this.presentationNames.put(lang, name);
    }

    public void addDescription(final String lang, final String description) {
        this.descriptions.put(lang, description);
    }

    public Map<String, String> getDescriptions() {
        return descriptions;
    }

    public Map<String, String> getPresentationNames() {
        return presentationNames;
    }
}
