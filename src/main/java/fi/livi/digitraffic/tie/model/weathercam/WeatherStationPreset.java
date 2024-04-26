package fi.livi.digitraffic.tie.model.weathercam;

import java.time.Instant;

public interface WeatherStationPreset {
    String getPresetId();

    Instant getPictureLastModified();

    String getCameraId();

    Instant getPicLastModifiedDb();
}
