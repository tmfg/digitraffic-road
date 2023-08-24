package fi.livi.digitraffic.tie.model.v1.camera;

import java.time.Instant;

public interface WeatherStationPreset {
    String getPresetId();

    Instant getPictureLastModified();

    String getCameraId();
}
