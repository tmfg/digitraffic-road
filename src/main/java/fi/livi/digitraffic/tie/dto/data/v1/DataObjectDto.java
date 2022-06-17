package fi.livi.digitraffic.tie.dto.data.v1;

import java.io.Serializable;
import java.time.Instant;

import org.hibernate.annotations.Immutable;

import fi.livi.digitraffic.tie.dto.v1.camera.CameraRootDataObjectDto;
import fi.livi.digitraffic.tie.dto.v1.freeflowspeed.FreeFlowSpeedRootDataObjectDto;
import fi.livi.digitraffic.tie.dto.v1.tms.TmsRootDataObjectDto;
import fi.livi.digitraffic.tie.dto.v1.tms.TmsSensorConstantRootDto;
import fi.livi.digitraffic.tie.dto.v1.weather.WeatherRootDataObjectDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Immutable
@Schema(subTypes = { CameraRootDataObjectDto.class, TmsRootDataObjectDto.class, TmsSensorConstantRootDto.class, FreeFlowSpeedRootDataObjectDto.class, WeatherRootDataObjectDto.class })
public abstract class DataObjectDto implements Serializable {

    @Schema(description = "Time when data was last updated", required = true)
    public final Instant dataUpdatedTime;

    public DataObjectDto(final Instant dataUpdatedTime) {
        this.dataUpdatedTime = dataUpdatedTime;
    }
}
