package fi.livi.digitraffic.tie.dto.v1;

import java.io.Serializable;
import java.time.ZonedDateTime;

import org.hibernate.annotations.Immutable;

import fi.livi.digitraffic.tie.dto.v1.camera.CameraRootDataObjectDto;
import fi.livi.digitraffic.tie.dto.v1.tms.TmsRootDataObjectDto;
import fi.livi.digitraffic.tie.dto.v1.tms.TmsSensorConstantRootDto;
import fi.livi.digitraffic.tie.dto.v1.weather.WeatherRootDataObjectDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Immutable
@Schema(subTypes = { CameraRootDataObjectDto.class, TmsRootDataObjectDto.class, TmsSensorConstantRootDto.class, WeatherRootDataObjectDto.class })
public class RootDataObjectDto implements Serializable {

    @Schema(description = "Data last updated date time", required = true)
    public final ZonedDateTime dataUpdatedTime;

    public RootDataObjectDto(final ZonedDateTime dataUpdatedTime) {
        this.dataUpdatedTime = dataUpdatedTime;
    }
}
