package fi.livi.digitraffic.tie.converter.weathercam.v1;

import static java.util.stream.Collectors.toList;

import java.time.Instant;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamPresetDataV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamStationDataV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamStationsDataV1;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.weathercam.CameraPreset;
import fi.livi.digitraffic.tie.model.weathercam.WeatherStationPreset;

@ConditionalOnWebApplication
@Component
public class WeathercamDataConverter {
    @Autowired
    public WeathercamDataConverter() {
    }

    public WeathercamStationsDataV1 convert(final List<CameraPreset> cameraPresets,
                                            final Instant updated) {
        final Map<String, List<WeathercamPresetDataV1>> cameraIdToPresetDataMap =
            cameraPresets.stream()
                .map(cp -> new AbstractMap.SimpleEntry<>(
                    cp.getCameraId(),
                    new WeathercamPresetDataV1(cp.getPresetId(), DateHelper.toInstantWithOutMillis(cp.getPictureLastModified()))))
                .collect(Collectors.groupingBy(Map.Entry::getKey, LinkedHashMap::new, // LinkedHashMap keeps the order
                    Collectors.mapping(Map.Entry::getValue, toList())));

        final List<WeathercamStationDataV1> stationsDatas =
            cameraIdToPresetDataMap.entrySet().stream()
                .map(e -> new WeathercamStationDataV1(e.getKey(), e.getValue(), getMaxMeasuredTime(e.getValue())))
                .collect(toList());

        return new WeathercamStationsDataV1(updated, stationsDatas);
    }

    public WeathercamStationDataV1 convertSingleStationData(final List<WeatherStationPreset> presets) {
        final List<WeathercamPresetDataV1> presetsDatas = presets.stream()
            .map(preset -> new WeathercamPresetDataV1(preset.getPresetId(), DateHelper.toInstantWithOutMillis(preset.getPictureLastModified())))
            .collect(toList());
        final Instant dataUpdatedTime = getMaxPicLastModifiedDb(presets);

        return new WeathercamStationDataV1(presets.get(0).getCameraId(), presetsDatas, dataUpdatedTime);
    }

    private static Instant getMaxMeasuredTime(final List<WeathercamPresetDataV1> presetDatas) {
        return presetDatas.stream().filter(cp -> cp.measuredTime != null).map(cp -> cp.measuredTime).max(Instant::compareTo).orElse(null);
    }

    private static Instant getMaxPicLastModifiedDb(final List<WeatherStationPreset> presets) {
        return presets.stream().filter(cp -> cp.getPicLastModifiedDb() != null).map(cp -> cp.getPicLastModifiedDb()).max(Instant::compareTo).orElse(null);
    }
}


