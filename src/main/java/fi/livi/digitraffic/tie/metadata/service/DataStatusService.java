package fi.livi.digitraffic.tie.metadata.service;

import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.dao.DataUpdatedRepository;
import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.model.DataUpdated;

@Service
public class DataStatusService {
    private static final Logger log = LoggerFactory.getLogger(DataStatusService.class);

    public enum StaticStatusType {
        TMS("LAM_DATA_LAST_UPDATED"),
        ROAD_WEATHER("RWS_DATA_LAST_UPDATED"),
        ROAD_WEATHER_SENSOR("RW_SENSOR_DATA_LAST_UPDATED"),
        CAMERA_PRESET("CAMERAPRESET_DATA_LAST_UPDATED"),
        LINK("LINK_DATA_LAST_UPDATED");

        private final String updateField;

        StaticStatusType(final String updateField) {
            this.updateField = updateField;
        }
    }

    private final DataUpdatedRepository dataUpdatedRepository;

    @Autowired
    public DataStatusService(final DataUpdatedRepository dataUpdatedRepository) {
        this.dataUpdatedRepository = dataUpdatedRepository;
    }

    @Transactional
    public void updateDataUpdated(final DataType dataType) {
        updateDataUpdated(dataType, (String)null);
    }

    @Transactional
    public void updateDataUpdated(final DataType dataType, final String version) {
        final DataUpdated updated = dataUpdatedRepository.findByDataType(dataType.name());
        log.info("Update DataUpdated, type={}, version={}", dataType, version);

        if (updated == null) {
            dataUpdatedRepository.save(new DataUpdated(dataType, ZonedDateTime.now(), version));
        } else {
            updated.setUpdatedTime(ZonedDateTime.now());
            updated.setVersion(version);
        }
    }

    @Transactional
    public void updateDataUpdated(final DataType dataType, final ZonedDateTime updated) {
        final DataUpdated dataUpdated = dataUpdatedRepository.findByDataType(dataType.name());
        log.info("Update DataUpdated, type={}, updated={}", dataType, updated);

        if (dataUpdated == null) {
            dataUpdatedRepository.save(new DataUpdated(dataType, updated, null));
        } else {
            dataUpdated.setUpdatedTime(updated);
        }
    }

    @Transactional(readOnly = true)
    public ZonedDateTime findDataUpdatedTimeByDataType(final DataType dataType) {
        final DataUpdated updated = dataUpdatedRepository.findByDataType(dataType.name());
        return updated != null ? updated.getUpdatedTime() : null;
    }
}
