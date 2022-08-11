package fi.livi.digitraffic.tie.dto.tms.v1;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.roadstation.v1.StationsDataV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Latest sensor constant values of TMS stations")
@JsonPropertyOrder({ "dataUpdatedTime", "stations"})
public class TmsStationsSensorConstantsDataDtoV1 extends StationsDataV1<Long, TmsStationSensorConstantDtoV1> {

    public TmsStationsSensorConstantsDataDtoV1(final Instant dataUpdatedTime, final List<TmsStationSensorConstantDtoV1> sensorConstants) {
        super(dataUpdatedTime, sensorConstants);
    }
}