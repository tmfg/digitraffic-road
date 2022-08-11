package fi.livi.digitraffic.tie.dto.data.v1;

import java.io.Serializable;
import java.time.Instant;

import fi.livi.digitraffic.tie.dto.v1.camera.CameraRootDataObjectDto;
import fi.livi.digitraffic.tie.dto.v1.freeflowspeed.FreeFlowSpeedRootDataObjectDto;
import fi.livi.digitraffic.tie.dto.v1.tms.TmsRootDataObjectDto;
import fi.livi.digitraffic.tie.dto.v1.tms.TmsSensorConstantRootDto;
import fi.livi.digitraffic.tie.dto.v1.weather.WeatherRootDataObjectDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(subTypes = { CameraRootDataObjectDto.class, TmsRootDataObjectDto.class, TmsSensorConstantRootDto.class, FreeFlowSpeedRootDataObjectDto.class, WeatherRootDataObjectDto.class })
public class RootDataDtoV1 implements Serializable {

    @Schema(description = "Data last updated date time", required = true)
    public final Instant dataUpdatedTime;

    public RootDataDtoV1(final Instant dataUpdatedTime) {
        this.dataUpdatedTime = dataUpdatedTime;
    }
}
