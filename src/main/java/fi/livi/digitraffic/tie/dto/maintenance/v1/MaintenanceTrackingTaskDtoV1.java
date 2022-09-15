package fi.livi.digitraffic.tie.dto.maintenance.v1;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Maintenance tracking task")
@JsonPropertyOrder({ "id", "nameFi", "nameEn", "nameSv"})
public class MaintenanceTrackingTaskDtoV1 {

    public final String id;
    public final String nameFi;
    public final String nameSv;
    public final String nameEn;

    public MaintenanceTrackingTaskDtoV1(final String id, final String nameFi, final String nameSv, final String nameEn) {
        this.id = id;
        this.nameFi = nameFi;
        this.nameSv = nameSv;
        this.nameEn = nameEn;
    }
}