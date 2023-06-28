package fi.livi.digitraffic.tie.dto.trafficmessage.v1.location;

import java.time.Instant;

import fi.livi.digitraffic.tie.dto.data.v1.DataUpdatedSupportV1;
import fi.livi.digitraffic.tie.helper.LocationUtils;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

@Schema(description = "Location Version Object")
public class LocationVersionDtoV1 implements DataUpdatedSupportV1, Comparable<LocationVersionDtoV1> {

    @Schema(description = "Location version string", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    public final String version;

    private final Instant dataUpdatedTime;

    public LocationVersionDtoV1(final String version, final Instant dataUpdatedTime) {
        this.version = version;
        this.dataUpdatedTime = dataUpdatedTime;
    }

    @Override
    public Instant getDataUpdatedTime() {
        return dataUpdatedTime;
    }

    @Override
    public int compareTo(final LocationVersionDtoV1 o) {
        return LocationUtils.compareTypesOrVersions(version, o.version);
    }
}
