package fi.livi.digitraffic.tie.dto.maintenance.v1;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonPropertyOrder({ "name", "source" })
@ApiModel(description = "Maintenance tracking domain", value = "Domain_V1")
public interface DomainDto {

    @ApiModelProperty(value = "Name of the maintenance tracking domain", required = true)
    @NotNull
    String getName();

    @ApiModelProperty(value = "Source and owner of the data", required = true)
    @NotNull
    String getSource();
}
