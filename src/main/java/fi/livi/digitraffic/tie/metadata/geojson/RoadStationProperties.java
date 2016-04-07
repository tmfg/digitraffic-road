package fi.livi.digitraffic.tie.metadata.geojson;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Properties", description = "Roadstation properties")
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class RoadStationProperties {

    @ApiModelProperty(value = "Road station's natural id")
    private long naturalId;

    @ApiModelProperty(value = "Common name of road station")
    private String name;

    @ApiModelProperty(value = "Road number")
    private Integer roadNumber;

    @ApiModelProperty(value = "Road part")
    private Integer roadPart;

    @ApiModelProperty(value = "Distance from start of the road part in meters")
    private Integer distance;

    @ApiModelProperty(value = "Data collection interval in seconds")
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

    @ApiModelProperty(value = "Map of namess in fi, sv, en")
    private Map<String, String> names = new HashMap<>();

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

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(final Integer distance) {
        this.distance = distance;
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


}
