package fi.livi.digitraffic.tie.metadata.geojson.roadstation;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Properties", description = "Roadstation properties")
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class RoadStationProperties {

    @JsonIgnore
    @ApiModelProperty(value = "Road station's lotju id")
    private Long lotjuId;

    @ApiModelProperty(value = "Road station's natural id", required = true)
    @JsonProperty("roadStationId")
    private long naturalId;

    @ApiModelProperty(value = "Common name of road station")
    private String name;

    @ApiModelProperty(value = "Road number")
    private Integer roadNumber;

    @ApiModelProperty(value = "Road part")
    private Integer roadPart;

    @ApiModelProperty(value = "Distance from start of the road part [m]")
    private Integer distanceFromRoadPartStart;

    @ApiModelProperty(value = "Data collection interval [s]")
    private Integer collectionInterval;

    @ApiModelProperty(value = "Data collection status")
    private CollectionStatus collectionStatus;

    @ApiModelProperty(value = "Municipality")
    private String municipality;

    @ApiModelProperty(value = "Municipality code")
    private String municipalityCode;

    @ApiModelProperty(value = "Province")
    private String province;

    @ApiModelProperty(value = "Province code")
    private String provinceCode;

    @ApiModelProperty(value = "Description")
    private String description;

    @ApiModelProperty(value = "Additional information")
    private String additionalInformation;

    @ApiModelProperty(value = "Map of namess [fi, sv, en]")
    private Map<String, String> names = new HashMap<>();

    public long getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(final long naturalId) {
        this.naturalId = naturalId;
    }

    public Long getLotjuId() {
        return lotjuId;
    }

    public void setLotjuId(Long lotjuId) {
        this.lotjuId = lotjuId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Integer getRoadNumber() {
        return roadNumber;
    }

    public void setRoadNumber(final Integer roadNumber) {
        this.roadNumber = roadNumber;
    }

    public Integer getRoadPart() {
        return roadPart;
    }

    public void setRoadPart(final Integer roadPart) {
        this.roadPart = roadPart;
    }

    public Integer getDistanceFromRoadPartStart() {
        return distanceFromRoadPartStart;
    }

    public void setDistanceFromRoadPartStart(final Integer distanceFromRoadPartStart) {
        this.distanceFromRoadPartStart = distanceFromRoadPartStart;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        RoadStationProperties rhs = (RoadStationProperties) obj;
        return new EqualsBuilder()
                .append(this.lotjuId, rhs.lotjuId)
                .append(this.naturalId, rhs.naturalId)
                .append(this.name, rhs.name)
                .append(this.roadNumber, rhs.roadNumber)
                .append(this.roadPart, rhs.roadPart)
                .append(this.distanceFromRoadPartStart, rhs.distanceFromRoadPartStart)
                .append(this.collectionInterval, rhs.collectionInterval)
                .append(this.collectionStatus, rhs.collectionStatus)
                .append(this.municipality, rhs.municipality)
                .append(this.municipalityCode, rhs.municipalityCode)
                .append(this.province, rhs.province)
                .append(this.provinceCode, rhs.provinceCode)
                .append(this.description, rhs.description)
                .append(this.additionalInformation, rhs.additionalInformation)
                .append(this.names, rhs.names)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(lotjuId)
                .append(naturalId)
                .append(name)
                .append(roadNumber)
                .append(roadPart)
                .append(distanceFromRoadPartStart)
                .append(collectionInterval)
                .append(collectionStatus)
                .append(municipality)
                .append(municipalityCode)
                .append(province)
                .append(provinceCode)
                .append(description)
                .append(additionalInformation)
                .append(names)
                .toHashCode();
    }
}
