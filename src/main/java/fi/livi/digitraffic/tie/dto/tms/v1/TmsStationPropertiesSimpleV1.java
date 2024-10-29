package fi.livi.digitraffic.tie.dto.tms.v1;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.RoadStationPropertiesSimpleV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tms station properties object with basic information")
@JsonPropertyOrder({ "id", "tmsNumber", "name" })
public class TmsStationPropertiesSimpleV1 extends RoadStationPropertiesSimpleV1<Long> {

    // NaturalId of tms station
    @Schema(description = "TMS station number (naturalId) for legacy support", requiredMode = Schema.RequiredMode.REQUIRED)
    public final long tmsNumber;

    public TmsStationPropertiesSimpleV1(final long id, final long tmsNumber) {
        super(id);
        this.tmsNumber = tmsNumber;
    }
}
