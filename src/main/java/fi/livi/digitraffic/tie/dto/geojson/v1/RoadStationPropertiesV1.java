package fi.livi.digitraffic.tie.dto.geojson.v1;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fi.livi.digitraffic.tie.metadata.geojson.Properties;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraProperties;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationProperties;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationProperties;
import fi.livi.digitraffic.tie.model.CollectionStatus;
import fi.livi.digitraffic.tie.model.RoadStationState;
import fi.livi.digitraffic.tie.model.v1.RoadAddress;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Properties", description = "Roadstation properties", subTypes = { CameraProperties.class, TmsStationProperties.class, WeatherStationProperties.class })
public abstract class RoadStationPropertiesV1<ID_TYPE> extends Properties {

    @Schema(description = "Common name of road station")
    private String name;

    @Schema(description = "Data collection interval [s]")
    private Integer collectionInterval;

    @Schema(description = "Data collection status")
    private CollectionStatus collectionStatus;

    @Schema(description = "Municipality")
    private String municipality;

    @Schema(description = "Municipality code")
    private String municipalityCode;

    @Schema(description = "Province")
    private String province;

    @Schema(description = "Province code")
    private String provinceCode;

    @Schema(description = "Map of names [fi, sv, en]",
                      example = "\"names\": {\n" +
                                "          \"fi\": \"Tie 7 Porvoo\",\n" +
                                "          \"sv\": \"Väg 7 Borgå\",\n" +
                                "          \"en\": \"Road 7 Porvoo\"\n" +
                                "        },")
    private Map<String, String> names = new HashMap<>();

    private RoadAddress roadAddress = new RoadAddress();

    @Schema(description = "Id in road registry")
    private String liviId;

    @Schema(description = "Country where station is located")
    private String country;

    @Schema(description = "Station established date time")
    private ZonedDateTime startTime;

    @Schema(description = "Repair maintenance date time")
    private ZonedDateTime repairMaintenanceTime;

    @Schema(description = "Annual maintenance date time")
    private ZonedDateTime annualMaintenanceTime;

    // Removed temporary until LOTJU data is fixed
    @JsonIgnore
    @Schema(description = "Location of the station")
    private String location;

    @Schema(description = "Road station state")
    private RoadStationState state;

    @Schema(description = "Purpose of the road station")
    private String purpose;

    @Schema(description = "Station id", required = true)
    public abstract ID_TYPE getId();
    public abstract void setId(final ID_TYPE id);

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Integer getCollectionInterval() {
        return collectionInterval;
    }

    public void setCollectionInterval(final Integer collectionInterval) {
        this.collectionInterval = collectionInterval;
    }

    public CollectionStatus getCollectionStatus() {
        return collectionStatus;
    }

    public void setCollectionStatus(final CollectionStatus collectionStatus) {
        this.collectionStatus = collectionStatus;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(final String municipality) {
        this.municipality = municipality;
    }

    public String getMunicipalityCode() {
        return municipalityCode;
    }

    public void setMunicipalityCode(final String municipalityCode) {
        this.municipalityCode = municipalityCode;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(final String province) {
        this.province = province;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(final String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public Map<String, String> getNames() {
        return names;
    }

    public void setNames(final Map<String, String> names) {
        this.names = names;
    }

    public void addName(final String lang, final String name) {
        if (name != null) {
            this.names.put(lang, name);
        }
    }

    public RoadAddress getRoadAddress() {
        return roadAddress;
    }

    public void setRoadAddress(final RoadAddress roadAddress) {
        this.roadAddress = roadAddress;
    }

    public void setLiviId(final String liviId) {
        this.liviId = liviId;
    }

    public String getLiviId() {
        return liviId;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public void setStartTime(final ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setState(final RoadStationState state) {
        this.state = state;
    }

    public RoadStationState getState() {
        return state;
    }

    public void setRepairMaintenanceTime(final ZonedDateTime repairMaintenanceTime) {
        this.repairMaintenanceTime = repairMaintenanceTime;
    }

    public ZonedDateTime getRepairMaintenanceTime() {
        return repairMaintenanceTime;
    }

    public void setAnnualMaintenanceTime(final ZonedDateTime annualMaintenanceTime) {
        this.annualMaintenanceTime = annualMaintenanceTime;
    }

    public ZonedDateTime getAnnualMaintenanceTime() {
        return annualMaintenanceTime;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final RoadStationPropertiesV1<?> that = (RoadStationPropertiesV1<?>) o;

        return new EqualsBuilder()
                .append(getId(), that.getId())
                .append(name, that.name)
                .append(collectionInterval, that.collectionInterval)
                .append(collectionStatus, that.collectionStatus)
                .append(municipality, that.municipality)
                .append(municipalityCode, that.municipalityCode)
                .append(province, that.province)
                .append(provinceCode, that.provinceCode)
                .append(names, that.names)
                .append(roadAddress, that.roadAddress)
                .append(liviId, that.liviId)
                .append(country, that.country)
                .append(startTime, that.startTime)
                .append(location, that.location)
                .append(state, that.state)
                .append(repairMaintenanceTime, that.repairMaintenanceTime)
                .append(annualMaintenanceTime, that.annualMaintenanceTime)
                .append(purpose, that.purpose)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getId())
                .append(name)
                .append(collectionInterval)
                .append(collectionStatus)
                .append(municipality)
                .append(municipalityCode)
                .append(province)
                .append(provinceCode)
                .append(names)
                .append(roadAddress)
                .append(liviId)
                .append(country)
                .append(startTime)
                .append(location)
                .append(state)
                .append(repairMaintenanceTime)
                .append(annualMaintenanceTime)
                .append(purpose)
                .toHashCode();
    }
}
