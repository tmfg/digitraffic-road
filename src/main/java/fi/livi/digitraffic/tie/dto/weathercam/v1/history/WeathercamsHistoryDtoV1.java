package fi.livi.digitraffic.tie.dto.weathercam.v1.history;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import fi.livi.digitraffic.tie.dto.roadstation.v1.StationsDataV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CameraHistory", description = "Weather camera's image history details.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeathercamsHistoryDtoV1 extends StationsDataV1<String, WeathercamPresetsHistoryDtoV1> {

    public WeathercamsHistoryDtoV1(final Instant dataUpdatedTime, final List<WeathercamPresetsHistoryDtoV1> stationsHistory) {
        super(dataUpdatedTime, stationsHistory);
    }

}
