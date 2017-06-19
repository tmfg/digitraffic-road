package fi.livi.digitraffic.tie.metadata.service;

import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.dao.MetadataUpdatedRepository;
import fi.livi.digitraffic.tie.metadata.dao.StaticDataStatusDAO;
import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;

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
    private final MetadataUpdatedRepository metadataUpdatedRepository;

    @Autowired
    public DataStatusService(final StaticDataStatusDAO staticDataStatusDAO,
                             final MetadataUpdatedRepository metadataUpdatedRepository) {
        this.staticDataStatusDAO = staticDataStatusDAO;
        this.metadataUpdatedRepository = metadataUpdatedRepository;
    }

    @Transactional
    public void updateStaticDataStatus(final StaticStatusType type, final boolean updateStaticDataStatus) {
        staticDataStatusDAO.updateStaticDataStatus(type, updateStaticDataStatus);
    }

    @Transactional
    public void updateDataUpdated(final DataType dataType) {
        updateDataUpdated(dataType, null);
    }

    @Transactional
    public void updateDataUpdated(final DataType dataType, final String version) {
        final MetadataUpdated updated = metadataUpdatedRepository.findByMetadataType(dataType.name());
        log.info("Update MetadataUpdated, type: " + dataType + ", version: " + version);
        if (updated == null) {
            metadataUpdatedRepository.save(new MetadataUpdated(dataType, ZonedDateTime.now(), version));
        } else {
            updated.setUpdatedTime(ZonedDateTime.now());
            updated.setVersion(version);
        }
    }

    @Transactional
    public void setMetadataUpdated(final DataType dataType, ZonedDateTime updated) {
        MetadataUpdated metadataUpdated = metadataUpdatedRepository.findByMetadataType(dataType.name());
        if (metadataUpdated == null) {
            metadataUpdatedRepository.save(new MetadataUpdated(dataType, updated, null));
        } else {
            metadataUpdated.setUpdatedTime(updated);
        }
    }

    @Transactional(readOnly = true)
    public MetadataUpdated findMetadataUpdatedByMetadataType(final DataType dataType) {
        return metadataUpdatedRepository.findByMetadataType(dataType.name());
    }
}
