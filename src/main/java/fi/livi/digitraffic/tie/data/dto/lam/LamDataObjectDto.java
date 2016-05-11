package fi.livi.digitraffic.tie.data.dto.lam;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.DataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Latest measurement data from LAM stations.")
@JsonPropertyOrder({ "dataLocalTime", "dataUtc", "lamData"})
public class LamDataObjectDto extends DataObjectDto {

    @ApiModelProperty(value = "Latest measurement data from LAM stations", required = true)
    private final List<LamMeasurementDto> lamData;

    public LamDataObjectDto(final List<LamMeasurementDto> lamData) {
        this.lamData = lamData;
    }

    public List<LamMeasurementDto> getLamData() {
        return lamData;
    }
}
