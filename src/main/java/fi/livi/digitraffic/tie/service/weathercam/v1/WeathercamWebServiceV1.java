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
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamFeatureCollectionV1;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraStationFeature;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPreset;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetService;

@ConditionalOnWebApplication
@Service
public class WeathercamWebServiceV1 {
    private static final Logger log = LoggerFactory.getLogger(WeathercamWebServiceV1.class);

    private final WeathercamPresetToFeatureConverter weathercamPresetToFeatureConverter;
    private final CameraPresetService cameraPresetService;
    private final DataStatusService dataStatusService;

    @Autowired
    public WeathercamWebServiceV1(final WeathercamPresetToFeatureConverter weathercamPresetToFeatureConverter,
                                  final CameraPresetService cameraPresetService,
                                  final DataStatusService dataStatusService) {
        this.weathercamPresetToFeatureConverter = weathercamPresetToFeatureConverter;
        this.cameraPresetService = cameraPresetService;
        this.dataStatusService = dataStatusService;
    }

    @Transactional(readOnly = true)
    public WeathercamFeatureCollectionV1 findAllPublishableCameraStationsAsFeatureCollection(final boolean onlyUpdateInfo) {
        return weathercamPresetToFeatureConverter.convert(
                onlyUpdateInfo ?
                Collections.emptyList() :
                cameraPresetService.findAllPublishableCameraPresets(),
                dataStatusService.findDataUpdatedInstant(DataType.CAMERA_STATION_METADATA),
                dataStatusService.findDataUpdatedInstant(DataType.CAMERA_STATION_METADATA_CHECK));
    }

    @Transactional(readOnly = true)
    public CameraStationFeature findPublishableCameraStationAsFeature(final String stationId) {
        // TODO
        final List<CameraPreset> resutl =
            cameraPresetService.findAllPublishableCameraPresetsByCameraId(stationId);
        return null;
    }
}