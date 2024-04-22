package fi.livi.digitraffic.tie.dto.roadstation.v1;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import fi.livi.digitraffic.common.dto.LastModifiedSupport;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamStationsDataV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Road stations datas base", subTypes = { WeathercamStationsDataV1.class })
public abstract class StationsDataV1<ID_TYPE, StationsDataType extends StationDataV1<ID_TYPE>> implements Serializable, LastModifiedSupport {

    @Schema(description = "Time when data was last updated", required = true)
    public final Instant dataUpdatedTime;

    @Schema(description = "Stations data")
    public final List<StationsDataType> stations;

    public StationsDataV1(final Instant dataUpdatedTime, final List<StationsDataType> stations) {
        this.dataUpdatedTime = dataUpdatedTime;
        this.stations = stations;
    }

    public StationsDataV1(final Instant dataUpdatedTime) {
        this(dataUpdatedTime, null);
    }

    @Override
    public Instant getLastModified() {
        return dataUpdatedTime;
    }

    @Override
    public boolean shouldContainLastModified() {
        return stations != null && !stations.isEmpty();
    }
}
