package fi.livi.digitraffic.tie.dto.v2.maintenance;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Maintenance tracking domain", value = "Domain")
public interface DomainDto {

    @ApiModelProperty(value = "Name of the maintenance tracking domain", required = true)
    @NotNull
    String getName();
}
