package fi.livi.digitraffic.tie.dto.trafficmessage.v1.location;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.data.v1.DataUpdatedSupportV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Location types and location subtypes")
@JsonPropertyOrder({ "dataUpdatedTime", "typesVersion" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocationTypesDtoV1 implements DataUpdatedSupportV1 {

    public final String version;

    private final Instant dataUpdatedTime;

    @Schema(description = "Location types")
    public final List<LocationTypeDtoV1> locationTypes;

    @Schema(description = "Location subtypes")
    public final List<LocationSubtypeDtoV1> locationSubtypes;

    public LocationTypesDtoV1(final Instant typesUpdated,
                              final String version,
                              final List<LocationTypeDtoV1> locationTypes,
                              final List<LocationSubtypeDtoV1> locationSubtypes) {
        this.dataUpdatedTime = typesUpdated;
        this.version = version;
        this.locationTypes = locationTypes;
        this.locationSubtypes = locationSubtypes;
    }

    public LocationTypesDtoV1(final Instant typesUpdated, final String typesVersion) {
        this(typesUpdated, typesVersion, null, null);
    }

    @Override
    public Instant getDataUpdatedTime() {
        return dataUpdatedTime;
    }
}
