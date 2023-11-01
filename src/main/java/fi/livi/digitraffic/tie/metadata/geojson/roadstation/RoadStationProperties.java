package fi.livi.digitraffic.tie.metadata.geojson.roadstation;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.Properties;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraProperties;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationProperties;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationProperties;
import fi.livi.digitraffic.tie.model.roadstation.CollectionStatus;
import fi.livi.digitraffic.tie.model.roadstation.RoadAddress;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationState;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Properties", description = "Roadstation properties", subTypes = { CameraProperties.class, TmsStationProperties.class, WeatherStationProperties.class })
public abstract class RoadStationProperties extends Properties {

    private static final int LONGITUDE_IDX = 0;
    private static final int LATITUDE_IDX = 1;
    private static final int ALTITUDE_IDX = 2;

    @JsonIgnore
    @Schema(description = "Road station's lotju id")
    private Long lotjuId;

    @Schema(description = "Road station's id (naturalId)", required = true)
    @JsonProperty("roadStationId")
    private long naturalId;

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
                      example = """
                              {
                                    "fi": "Tie 7 Porvoo, Harabacka",
                                    "sv": "Väg 7 Borgå, Harabacka",
                                    "en": "Road 7 Porvoo, Harabacka"
                              }""")
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

    @Schema(description = "Road station coordinates [LONGITUDE, LATITUDE, {ALTITUDE}]. Coordinates are in ETRS89 / ETRS-TM35FIN format. " +
                              "Altitude is optional and measured in metres.)", required = true)
    private final List<Double> coordinatesETRS89 = new ArrayList<>(3);

    public long getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(final long naturalId) {
        this.naturalId = naturalId;
    }

    public Long getLotjuId() {
        return lotjuId;
    }

    public void setLotjuId(final Long lotjuId) {
        this.lotjuId = lotjuId;
    }

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

    public void setPurpose(final String purpose) {
        this.purpose = purpose;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final RoadStationProperties that = (RoadStationProperties) o;

        return new EqualsBuilder()
                .append(naturalId, that.naturalId)
                .append(lotjuId, that.lotjuId)
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
                .append(coordinatesETRS89, that.coordinatesETRS89)
                .append(purpose, that.purpose)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(lotjuId)
                .append(naturalId)
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
                .append(coordinatesETRS89)
                .append(purpose)
                .toHashCode();
    }

    public void setCoordinatesETRS89(final Point coordinatesETRS89Point) {
        this.coordinatesETRS89.clear();
        if (coordinatesETRS89Point != null) {
            setCoordinateETRS89(LONGITUDE_IDX, coordinatesETRS89Point.getLongitude());
            setCoordinateETRS89(LATITUDE_IDX, coordinatesETRS89Point.getLatitude());
            setCoordinateETRS89(ALTITUDE_IDX, coordinatesETRS89Point.getAltitude());
        }
    }

    private void setCoordinateETRS89(final int index, final Double coordinate) {
        if (coordinate != null) {
            while (coordinatesETRS89.size() <= index) {
                coordinatesETRS89.add(null);
            }
            coordinatesETRS89.set(index, coordinate);
        }
    }

    private Double getCoordinateETRS89(int index) {
        if ( index < coordinatesETRS89.size() ) {
            return coordinatesETRS89.get(index);
        }
        return null;
    }

    public List<Double> getCoordinatesETRS89() {
        return coordinatesETRS89;
    }

    @JsonIgnore
    public Double getAltitudeETRS89() {
        return getCoordinateETRS89(ALTITUDE_IDX);
    }

    @JsonIgnore
    public Double getLongitudeETRS89() {
        return getCoordinateETRS89(LONGITUDE_IDX);
    }

    @JsonIgnore
    public Double getLatitudeETRS89() {
        return getCoordinateETRS89(LATITUDE_IDX);
    }
}
