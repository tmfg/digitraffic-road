package fi.livi.digitraffic.tie.service.weathercam.v1;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.converter.weathercam.v1.WeathercamPresetToFeatureConverter;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamStationFeatureCollectionSimpleV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamStationFeatureV1Detailed;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPreset;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetService;

@ConditionalOnWebApplication
@Service
public class WeathercamMetadataWebServiceV1 {
    private static final Logger log = LoggerFactory.getLogger(WeathercamMetadataWebServiceV1.class);

    private final WeathercamPresetToFeatureConverter weathercamPresetToFeatureConverter;
    private final CameraPresetService cameraPresetService;
    private final DataStatusService dataStatusService;

    @Autowired
    public WeathercamMetadataWebServiceV1(final WeathercamPresetToFeatureConverter weathercamPresetToFeatureConverter,
                                          final CameraPresetService cameraPresetService,
                                          final DataStatusService dataStatusService) {
        this.weathercamPresetToFeatureConverter = weathercamPresetToFeatureConverter;
        this.cameraPresetService = cameraPresetService;
        this.dataStatusService = dataStatusService;
    }

    @Transactional(readOnly = true)
    public WeathercamStationFeatureCollectionSimpleV1 findAllPublishableCameraStationsAsSimpleFeatureCollection(final boolean onlyUpdateInfo) {
        return weathercamPresetToFeatureConverter.convertToSimpleFeatureCollection(
                onlyUpdateInfo
                    ? Collections.emptyList()
                    : cameraPresetService.findAllPublishableCameraPresets(),
                dataStatusService.findDataUpdatedInstant(DataType.CAMERA_STATION_METADATA),
                dataStatusService.findDataUpdatedInstant(DataType.CAMERA_STATION_METADATA_CHECK));
    }

    @Transactional(readOnly = true)
    public WeathercamStationFeatureV1Detailed findPublishableCameraStationAsDetailedFeature(final String stationId) {
        final List<CameraPreset> presets =
            cameraPresetService.findAllPublishableCameraPresetsByCameraId(stationId);
        if (presets.isEmpty()) {
            throw new ObjectNotFoundException("weathercam station", stationId);
        }
        return weathercamPresetToFeatureConverter.convertToDetailedFeature(presets);
    }
}