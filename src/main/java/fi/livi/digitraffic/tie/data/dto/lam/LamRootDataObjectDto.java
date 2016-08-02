package fi.livi.digitraffic.tie.data.dto.lam;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "LamData", description = "Latest measurement data from LAM stations.", parent = RootDataObjectDto.class)
@JsonPropertyOrder({ "dataUptadedLocalTime", "dataUptadedUtc", "lamMeasurements"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LamRootDataObjectDto extends RootDataObjectDto {

    @ApiModelProperty(value = "Latest measurement data from LAM stations")
    private final List<LamMeasurementDto> lamMeasurements;

    public LamRootDataObjectDto(final List<LamMeasurementDto> lamMeasurements,
                                final LocalDateTime updated) {
        super(updated);
        this.lamMeasurements = lamMeasurements;
    }

    public LamRootDataObjectDto(final LocalDateTime updated) {
        this(null, updated);
    }

    public List<LamMeasurementDto> getLamMeasurements() {
        return lamMeasurements;
    }
}
