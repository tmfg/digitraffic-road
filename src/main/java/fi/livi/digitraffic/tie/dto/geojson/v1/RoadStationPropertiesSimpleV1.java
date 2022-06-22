package fi.livi.digitraffic.tie.dto.geojson.v1;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamStationPropertiesSimpleV1;
import fi.livi.digitraffic.tie.metadata.geojson.PropertiesWithId;
import fi.livi.digitraffic.tie.model.CollectionStatus;
import fi.livi.digitraffic.tie.model.RoadStationState;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Properties", description = "Roadstation properties",
        subTypes = { RoadStationPropertiesDetailedV1.class,
                     WeathercamStationPropertiesSimpleV1.class
                     /* TODO TmsStationPropertiesSimpleV1, WeatherStationPropertiesSimpleV1 */ })
public abstract class RoadStationPropertiesSimpleV1<ID_TYPE> extends PropertiesWithId<ID_TYPE> {

    @Schema(description = "Common name of road station")
    private String name;

    @Schema(description = "Data collection status")
    private CollectionStatus collectionStatus;

    @Schema(description = "Road station state")
    private RoadStationState state;

    @Schema(description = "Municipality")
    private String municipality;

    @Schema(description = "Municipality code")
    private String municipalityCode;

    @Schema(description = "Province")
    private String province;

    @Schema(description = "Province code")
    private String provinceCode;

    public RoadStationPropertiesSimpleV1(final ID_TYPE id) {
        super(id);
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
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

    public void setState(final RoadStationState state) {
        this.state = state;
    }

    public RoadStationState getState() {
        return state;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final RoadStationPropertiesSimpleV1<?> that = (RoadStationPropertiesSimpleV1<?>) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(name, that.name)
                .append(collectionStatus, that.collectionStatus)
                .append(state, that.state)
                .append(municipality, that.municipality)
                .append(municipalityCode, that.municipalityCode)
                .append(province, that.province)
                .append(provinceCode, that.provinceCode)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(name)
                .append(collectionStatus)
                .append(state)
                .append(municipality)
                .append(municipalityCode)
                .append(province)
                .append(provinceCode)
                .toHashCode();
    }
}
