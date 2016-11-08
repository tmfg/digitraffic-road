package fi.livi.digitraffic.tie.metadata.service;

import fi.livi.digitraffic.tie.metadata.dao.MetadataUpdatedRepository;
import fi.livi.digitraffic.tie.metadata.dao.StaticDataStatusDAO;
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class StaticDataStatusService {

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
    public StaticDataStatusService(final StaticDataStatusDAO staticDataStatusDAO,
                                   final MetadataUpdatedRepository metadataUpdatedRepository) {
        this.staticDataStatusDAO = staticDataStatusDAO;
        this.metadataUpdatedRepository = metadataUpdatedRepository;
    }

    @Transactional
    public void updateStaticDataStatus(final StaticStatusType type, final boolean updateStaticDataStatus) {
        staticDataStatusDAO.updateStaticDataStatus(type, updateStaticDataStatus);
    }

    @Transactional
    public void updateMetadataUpdated(final MetadataType metadataType) {
        MetadataUpdated updated = metadataUpdatedRepository.findByMetadataType(metadataType.name());
        if (updated == null) {
            updated = new MetadataUpdated(metadataType, LocalDateTime.now());
            metadataUpdatedRepository.save(updated);
        } else {
            updated.setUpdated(LocalDateTime.now());
        }
    }

    @Transactional
    public void setMetadataUpdated(final MetadataType metadataType, LocalDateTime updated) {
        MetadataUpdated metadataUpdated = metadataUpdatedRepository.findByMetadataType(metadataType.name());
        if (metadataUpdated == null) {
            metadataUpdated = new MetadataUpdated(metadataType, updated);
            metadataUpdatedRepository.save(metadataUpdated);
        } else {
            metadataUpdated.setUpdated(updated);
        }
    }

    @Transactional(readOnly = true)
    public MetadataUpdated findMetadataUpdatedByMetadataType(final MetadataType metadataType) {
        return metadataUpdatedRepository.findByMetadataType(metadataType.name());
    }

}
