package fi.livi.digitraffic.tie.service.weathercam.v1;

import static fi.livi.digitraffic.tie.model.DataType.CAMERA_STATION_IMAGE_UPDATED;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.common.annotation.NotTransactionalServiceMethod;
import fi.livi.digitraffic.tie.converter.weathercam.v1.WeathercamDataConverter;
import fi.livi.digitraffic.tie.dao.weathercam.CameraPresetRepository;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamStationDataV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamStationsDataV1;
import fi.livi.digitraffic.tie.model.weathercam.WeatherStationPreset;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;

@ConditionalOnWebApplication
@Service
public class WeathercamDataWebServiceV1 {
    private final CameraPresetRepository cameraPresetRepository;
    private final WeathercamDataConverter weathercamDataConverter;
    private final DataStatusService dataStatusService;

    @Autowired
    WeathercamDataWebServiceV1(final CameraPresetRepository cameraPresetRepository,
                               final WeathercamDataConverter weathercamDataConverter,
                               final DataStatusService dataStatusService) {
        this.cameraPresetRepository = cameraPresetRepository;
        this.weathercamDataConverter = weathercamDataConverter;
        this.dataStatusService = dataStatusService;
    }

    @Transactional(readOnly = true)
    public WeathercamStationsDataV1 findPublishableWeathercamStationsData(final boolean onlyUpdateInfo) {
        final Instant updated = dataStatusService.findDataUpdatedInstant(CAMERA_STATION_IMAGE_UPDATED);

        if (onlyUpdateInfo) {
            return new WeathercamStationsDataV1(updated);
        } else {
            return weathercamDataConverter.convert(
                    cameraPresetRepository.findByPublishableIsTrueAndRoadStationPublishableNowIsTrueOrderByPresetId(null),
                    updated);
        }
    }

    @NotTransactionalServiceMethod // readoonly transaction declared in repository
    public WeathercamStationDataV1 findPublishableWeathercamStationData(final String cameraId) {
        final List<WeatherStationPreset> presets = cameraPresetRepository
                .findByCameraIdAndPublishableIsTrueAndRoadStationPublishableNowIsTrueOrderByPresetId(cameraId);

        if (presets.isEmpty()) {
            throw new ObjectNotFoundException("CameraStation", cameraId);
        }
        return weathercamDataConverter.convertSingleStationData(presets);
    }
}
