package fi.livi.digitraffic.tie.model.v1.camera;

import java.time.Instant;

public interface WeathercamPresetHistoryDto {

    String getPresetId();

    String getVersionId();

    Instant getLastModified();

    Boolean getPublishable();

    Integer getSize();

    Instant getCreated();

    Instant getModified();
}
