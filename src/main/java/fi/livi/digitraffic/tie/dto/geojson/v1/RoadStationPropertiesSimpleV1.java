package fi.livi.digitraffic.tie.dto.geojson.v1;

import java.time.Instant;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.livi.digitraffic.tie.dto.LastModifiedSupport;
import fi.livi.digitraffic.tie.metadata.geojson.PropertiesWithId;
import fi.livi.digitraffic.tie.model.CollectionStatus;
import fi.livi.digitraffic.tie.model.RoadStationState;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Roadstation simple properties")
public abstract class RoadStationPropertiesSimpleV1<ID_TYPE> extends PropertiesWithId<ID_TYPE> implements LastModifiedSupport {

    @Schema(description = "Common name of road station")
    private String name;

    @Schema(description = "Data collection status")
    private CollectionStatus collectionStatus;

    @Schema(description = "Road station state")
    private RoadStationState state;

    @Schema(description = "Data last updated date time", required = true)
    private Instant dataUpdatedTime;

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
                .append(dataUpdatedTime, that.dataUpdatedTime)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(name)
                .append(collectionStatus)
                .append(state)
                .append(dataUpdatedTime)
                .toHashCode();
    }

    public Instant getDataUpdatedTime() {
        return dataUpdatedTime;
    }

    public void setDataUpdatedTime(final Instant dataUpdatedTime) {
        this.dataUpdatedTime = dataUpdatedTime;
    }

    @Override
    public Instant getLastModified() {
        return dataUpdatedTime;
    }
}
