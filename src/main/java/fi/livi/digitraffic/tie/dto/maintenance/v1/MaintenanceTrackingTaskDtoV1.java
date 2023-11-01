package fi.livi.digitraffic.tie.dto.maintenance.v1;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.data.v1.DataUpdatedSupportV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Maintenance tracking task")
@JsonPropertyOrder({ "id", "nameFi", "nameEn", "nameSv"})
public class MaintenanceTrackingTaskDtoV1 implements DataUpdatedSupportV1 {

    public final String id;
    public final String nameFi;
    public final String nameSv;
    public final String nameEn;
    private final Instant dataUpdatedTime;

    public MaintenanceTrackingTaskDtoV1(final String id, final String nameFi, final String nameSv, final String nameEn, final Instant dataUpdatedTime) {
        this.id = id;
        this.nameFi = nameFi;
        this.nameSv = nameSv;
        this.nameEn = nameEn;
        this.dataUpdatedTime = dataUpdatedTime;
    }

    @Override
    public Instant getDataUpdatedTime() {
        return dataUpdatedTime;
    }
}