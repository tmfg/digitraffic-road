package fi.livi.digitraffic.tie.data.dto.lam;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "LamData", description = "Latest measurement data from LAM stations.", parent = RootDataObjectDto.class)
@JsonPropertyOrder({ "dataLocalTime", "dataUtc", "lamMeasurements"})
public class LamRootDataObjectDto extends RootDataObjectDto {

    @ApiModelProperty(value = "Latest measurement data from LAM stations", required = true)
    private final List<LamMeasurementDto> lamMeasurements;

    public LamRootDataObjectDto(final List<LamMeasurementDto> lamMeasurements) {
        this.lamMeasurements = lamMeasurements;
    }

    public List<LamMeasurementDto> getLamMeasurements() {
        return lamMeasurements;
    }
}
