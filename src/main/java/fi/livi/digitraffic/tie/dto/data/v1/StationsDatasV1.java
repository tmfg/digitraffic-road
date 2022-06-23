package fi.livi.digitraffic.tie.dto.data.v1;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamStationsDatasV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Road stations datas base", subTypes = { WeathercamStationsDatasV1.class })
public abstract class StationsDatasV1<ID_TYPE, StationsDataType extends StationDataV1<ID_TYPE>> implements Serializable {

    @Schema(description = "Time when data was last updated", required = true)
    public final Instant dataUpdatedTime;

    @Schema(description = "Stations data")
    public final List<StationsDataType> stations;

    public StationsDatasV1(final Instant dataUpdatedTime, final List<StationsDataType> stations) {
        this.dataUpdatedTime = dataUpdatedTime;
        this.stations = stations;
    }

    public StationsDatasV1(final Instant dataUpdatedTime) {
        this.dataUpdatedTime = dataUpdatedTime;
        this.stations = null;
    }
}
