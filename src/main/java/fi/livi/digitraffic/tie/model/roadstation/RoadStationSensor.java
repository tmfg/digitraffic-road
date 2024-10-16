package fi.livi.digitraffic.tie.model.roadstation;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SortNatural;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Road station sensor")
@JsonPropertyOrder(value = {"id", "name", "shortName", "descriptionFi", "descriptionSv", "descriptionEn", "unit", "accuracy", "nameOld", "sensorValueDescriptions"})
@Entity
@DynamicUpdate
public class RoadStationSensor {

    /** These id:s are for station status sensors */
    protected static final Set<Long> STATUS_SENSORS_NATURAL_IDS_SET =
            new HashSet<>(Arrays.asList(60000L, 60002L));

    @JsonIgnore
    @Id
    @SequenceGenerator(name = "SEQ_ROAD_STATION_SENSOR", sequenceName = "SEQ_ROAD_STATION_SENSOR", allocationSize = 1)
    @GeneratedValue(generator = "SEQ_ROAD_STATION_SENSOR")
    private Long id;

    @JsonIgnore
    @NotNull
    private Long lotjuId;

    @Schema(description = "Sensor id")
    @JsonProperty("id")
    private long naturalId;

    @Schema(description = "Sensor old name. For new sensors will equal name. Will deprecate in future.")
    @JsonProperty(value = "nameOld")
    private String name;

    @Schema(description = "Unit of sensor value")
    private String unit;

    @JsonIgnore
    private LocalDate obsoleteDate;

    @Schema(description = "Sensor description [fi]")
    private String descriptionFi;

    @Schema(description = "Sensor description [sv]")
    private String descriptionSv;

    @Schema(description = "Sensor description [en]")
    private String descriptionEn;

    @Schema(description = "Sensor name [fi]")
    @JsonProperty(value = "name")
    private String nameFi;

    @Schema(description = "Short name for sensor [fi]")
    @JsonProperty(value = "shortName")
    private String shortNameFi;

    @Schema(description = "Sensor accuracy")
    private Integer accuracy;

    @Schema(description = "Possible additional descriptions for sensor values")
    @OneToMany(mappedBy = "sensorValueDescriptionPK.sensorId", cascade = CascadeType.ALL)
    @SortNatural
    private SortedSet<SensorValueDescription> sensorValueDescriptions;

    @JsonIgnore
    @Enumerated(EnumType.STRING)
    private RoadStationType roadStationType;

    @Schema(description = "Presentation name for sensor [fi]")
    private String presentationNameFi;

    @Schema(description = "Presentation name for sensor [sv]")
    private String presentationNameSv;

    @Schema(description = "Presentation name for sensor [en]")
    private String presentationNameEn;

    @JsonIgnore
    private boolean isPublic;

    /** Not up to date in source system */
    @Schema(description = "Vehicle class")
    @Enumerated(EnumType.STRING)
    private VehicleClass vehicleClass;

    /** Not up to date in source system */
    @Schema(description = "Lane of the sensor, 1st, 2nd, 3rd, etc.")
    private Integer lane;

    /** Normally not up to date in source system */
    @Schema(description = "Preset direction " +
        "(0 = Unknown direction. " +
        "1 = According to the road register address increasing direction. I.e. on the road 4 to Rovaniemi." +
        "2 = According to the road register address decreasing direction. I.e. on the road 4 to Helsinki.")
    private Integer direction;

    /**
     * This value is calculated by db so it's value is not
     * reliable if entity is modified after fetch from db.
     */
    @Column(updatable = false, insertable = false) // virtual column
    private boolean publishable;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getLotjuId() {
        return lotjuId;
    }

    public void setLotjuId(final Long lotjuId) {
        this.lotjuId = lotjuId;
    }

    public long getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(final long naturalId) {
        this.naturalId = naturalId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public LocalDate getObsoleteDate() {
        return obsoleteDate;
    }

    public void setObsoleteDate(final LocalDate obsoleteDate) {
        this.obsoleteDate = obsoleteDate;
    }

    /**
     * Makes sensor obsolete if it's not already
     *
     * @return true is state was changed
     */
    public boolean makeObsolete() {
        if (obsoleteDate == null) {
            obsoleteDate = LocalDate.now();
            return true;
        }
        return false;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(final String unit) {
        this.unit = unit;
    }

    @Schema(description = "Sensor description [fi]")
    public String getDescription() {
        return getDescriptionFi();
    }

    public String getDescriptionFi() {
        return descriptionFi;
    }

    public void setDescriptionFi(final String description) {
        this.descriptionFi = description;
    }

    public void setDescriptionSv(final String descriptionSv) {
        this.descriptionSv = descriptionSv;
    }

    public String getDescriptionSv() {
        return descriptionSv;
    }

    public void setDescriptionEn(final String descriptionEn) {
        this.descriptionEn = descriptionEn;
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public String getNameFi() {
        return nameFi;
    }

    public void setNameFi(final String nameFi) {
        this.nameFi = StringUtils.upperCase(nameFi);
    }

    public String getShortNameFi() {
        return shortNameFi;
    }

    public void setShortNameFi(final String shortNameFi) {
        this.shortNameFi = shortNameFi;
    }

    public Integer getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(final Integer accuracy) {
        this.accuracy = accuracy;
    }

    @JsonIgnore
    public boolean isStatusSensor() {
        return STATUS_SENSORS_NATURAL_IDS_SET.contains(naturalId);
    }

    @Override
    public String toString() {
        return new ToStringHelper(this)
                .appendField("id", getId())
                .appendField("lotjuId", this.getLotjuId())
                .appendField("naturalId", getNaturalId())
                .appendField("name", getName())
                .appendField("nameFi", getNameFi())
                .appendField("unit", getUnit())
                .appendField("roadStationType", getRoadStationType())
                .toString();
    }

    public SortedSet<SensorValueDescription> getSensorValueDescriptions() {
        return sensorValueDescriptions;
    }

    public void setSensorValueDescriptions(final SortedSet<SensorValueDescription> sensorValueDescriptions) {
        this.sensorValueDescriptions = sensorValueDescriptions;
    }

    public RoadStationType getRoadStationType() {
        return roadStationType;
    }

    public void setRoadStationType(final RoadStationType roadStationType) {
        if (this.roadStationType != null && !this.roadStationType.equals(roadStationType)) {
            throw new IllegalStateException("Cannot change roadStationType of RoadStationSensor from " +
                    this.roadStationType + " to " + roadStationType + ". (" + this + ")");
        }
        this.roadStationType = roadStationType;
    }

    public void setPresentationNameFi(final String presentationNameFi) {
        this.presentationNameFi = presentationNameFi;
    }

    public String getPresentationNameFi() {
        return presentationNameFi;
    }

    public void setPresentationNameSv(final String presentationNameSe) {
        this.presentationNameSv = presentationNameSe;
    }

    public String getPresentationNameSv() {
        return presentationNameSv;
    }

    public void setPresentationNameEn(final String presentationNameEn) {
        this.presentationNameEn = presentationNameEn;
    }

    public String getPresentationNameEn() {
        return presentationNameEn;
    }

    public void setPublic(final boolean isPublic) {
        this.isPublic = isPublic;
    }

    @JsonIgnore
    public boolean isPublic() {
        return isPublic;
    }

    public void setVehicleClass(final VehicleClass vehicleClass) {
        this.vehicleClass = vehicleClass;
    }

    public VehicleClass getVehicleClass() {
        return vehicleClass;
    }

    public void setLane(final Integer lane) {
        this.lane = lane;
    }

    public Integer getLane() {
        return lane;
    }

    public void setDirection(final Integer direction) {
        this.direction = direction;
    }

    public Integer getDirection() {
        return direction;
    }

    public boolean isPublishable() {
        return publishable;
    }

    public boolean isSpeedSensor() {
        return StringUtils.contains(getNameFi(), "NOPEUS");
    }

    public boolean isFlowSensor() {
        return StringUtils.contains(getNameFi(), "OHITUKSET");
    }

    /**
     * E.g. moving average measurement
     * @return is this moving measurement
     */
    public boolean isMovingMeasurement() {
        return StringUtils.contains(getNameFi(), "LIUKUVA");
    }
}
