package fi.livi.digitraffic.tie.dto.maintenance.v1;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Maintenance tracking task", value = "MaintenanceTrackingTask_V1")
@JsonPropertyOrder({ "id", "nameFi", "nameEn", "nameSv"})
public class MaintenanceTrackingTaskDto {

    public final String id;
    public final String nameFi;
    public final String nameSv;
    public final String nameEn;

    public MaintenanceTrackingTaskDto(final String id, final String nameFi, final String nameSv, final String nameEn) {
        this.id = id;
        this.nameFi = nameFi;
        this.nameSv = nameSv;
        this.nameEn = nameEn;
    }
}