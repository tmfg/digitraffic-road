package fi.livi.digitraffic.tie.dto.tms.v1;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.roadstation.v1.StationsDataV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Latest measurement data from TMS Stations")
@JsonPropertyOrder({ "dataUpdatedTime", "stations"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TmsStationsDataDtoV1 extends StationsDataV1<Long, TmsStationDataDtoV1> {

    public TmsStationsDataDtoV1(final List<TmsStationDataDtoV1> tmsStations, final Instant updated) {
        super(updated, tmsStations);
    }

    public TmsStationsDataDtoV1(final Instant updated) {
        this(null, updated);
    }
}