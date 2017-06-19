package fi.livi.digitraffic.tie.metadata.service;

import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.dao.DataUpdatedRepository;
import fi.livi.digitraffic.tie.metadata.dao.StaticDataStatusDAO;
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

        public String getUpdateField() {
            return updateField;
        }
    }

    private final StaticDataStatusDAO staticDataStatusDAO;
    private final DataUpdatedRepository dataUpdatedRepository;

    @Autowired
    public DataStatusService(final StaticDataStatusDAO staticDataStatusDAO,
                             final DataUpdatedRepository dataUpdatedRepository) {
        this.staticDataStatusDAO = staticDataStatusDAO;
        this.dataUpdatedRepository = dataUpdatedRepository;
    }

    @Transactional
    public void updateStaticDataStatus(final StaticStatusType type, final boolean updateStaticDataStatus) {
        staticDataStatusDAO.updateStaticDataStatus(type, updateStaticDataStatus);
    }

    @Transactional
    public void updateDataUpdated(final DataType dataType) {
        updateDataUpdated(dataType, (String)null);
    }

    @Transactional
    public void updateDataUpdated(final DataType dataType, final String version) {
        final DataUpdated updated = dataUpdatedRepository.findByDataType(dataType.name());
        log.info("Update DataUpdated, type: " + dataType + ", version: " + version);
        if (updated == null) {
            dataUpdatedRepository.save(new DataUpdated(dataType, ZonedDateTime.now(), version));
        } else {
            updated.setUpdatedTime(ZonedDateTime.now());
            updated.setVersion(version);
        }
    }

    @Transactional
    public void updateDataUpdated(final DataType dataType, ZonedDateTime updated) {
        DataUpdated dataUpdated = dataUpdatedRepository.findByDataType(dataType.name());
        log.info("Update DataUpdated, type: " + dataType + ", updated: " + updated);
        if (dataUpdated == null) {
            dataUpdatedRepository.save(new DataUpdated(dataType, updated, null));
        } else {
            dataUpdated.setUpdatedTime(updated);
        }
    }

    @Transactional(readOnly = true)
    public DataUpdated findMetadataUpdatedByMetadataType(final DataType dataType) {
        return dataUpdatedRepository.findByDataType(dataType.name());
    }
}
