package fi.livi.digitraffic.tie.dto.geojson.v1;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.livi.digitraffic.tie.dto.roadstation.v1.StationRoadAddressV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Road station detailed properties")
public abstract class RoadStationPropertiesDetailedV1<ID_TYPE> extends RoadStationPropertiesSimpleV1<ID_TYPE> {

    @Schema(description = "Data collection interval [s]")
    private Integer collectionInterval;

    @Schema(description = "Map of names [fi, sv, en]",
            example = "{\n" +
                      "      \"fi\": \"Tie 7 Porvoo, Harabacka\",\n" +
                      "      \"sv\": \"Väg 7 Borgå, Harabacka\",\n" +
                      "      \"en\": \"Road 7 Porvoo, Harabacka\"\n" +
                      "}")
    private Map<String, String> names = new HashMap<>();

    @Schema(description = "Road address of the station")
    private StationRoadAddressV1 roadAddress;

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


    @Schema(description = "Purpose of the road station")
    private String purpose;

    @Schema(description = "Municipality")
    private String municipality;

    @Schema(description = "Municipality code")
    private Integer municipalityCode;

    @Schema(description = "Province")
    private String province;

    @Schema(description = "Province code")
    private Integer provinceCode;

    public RoadStationPropertiesDetailedV1(final ID_TYPE id) {
        super(id);
    }

    public Integer getCollectionInterval() {
        return collectionInterval;
    }

    public void setCollectionInterval(final Integer collectionInterval) {
        this.collectionInterval = collectionInterval;
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

    public StationRoadAddressV1 getRoadAddress() {
        return roadAddress;
    }

    public void setRoadAddress(final StationRoadAddressV1 roadAddress) {
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

    public void setPurpose(final String purpose) {
        this.purpose = purpose;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(final String municipality) {
        this.municipality = municipality;
    }

    public Integer getMunicipalityCode() {
        return municipalityCode;
    }

    public void setMunicipalityCode(final Integer municipalityCode) {
        this.municipalityCode = municipalityCode;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(final String province) {
        this.province = province;
    }

    public Integer getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(final Integer provinceCode) {
        this.provinceCode = provinceCode;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final RoadStationPropertiesDetailedV1<?> that = (RoadStationPropertiesDetailedV1<?>) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(that))
                .append(collectionInterval, that.collectionInterval)
                .append(names, that.names)
                .append(roadAddress, that.roadAddress)
                .append(liviId, that.liviId)
                .append(country, that.country)
                .append(startTime, that.startTime)
                .append(repairMaintenanceTime, that.repairMaintenanceTime)
                .append(annualMaintenanceTime, that.annualMaintenanceTime)
                .append(purpose, that.purpose)
                .append(municipality, that.municipality)
                .append(municipalityCode, that.municipalityCode)
                .append(province, that.province)
                .append(provinceCode, that.provinceCode)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(collectionInterval)
                .append(names)
                .append(roadAddress)
                .append(liviId)
                .append(country)
                .append(startTime)
                .append(repairMaintenanceTime)
                .append(annualMaintenanceTime)
                .append(purpose)
                .append(municipality)
                .append(municipalityCode)
                .append(province)
                .append(provinceCode)
                .toHashCode();
    }
}
