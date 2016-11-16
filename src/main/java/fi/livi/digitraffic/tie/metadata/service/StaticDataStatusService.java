package fi.livi.digitraffic.tie.metadata.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.dao.MetadataUpdatedRepository;
import fi.livi.digitraffic.tie.metadata.dao.StaticDataStatusDAO;
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;
import fi.livi.digitraffic.tie.metadata.service.location.MetadataVersions;

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

    public void updateMetadataUpdated(final MetadataType metadataType) {
        updateMetadataUpdated(metadataType, null);
    }

    @Transactional
    public void updateMetadataUpdated(final MetadataType metadataType, final String version) {
        final MetadataUpdated updated = metadataUpdatedRepository.findByMetadataType(metadataType.name());

        if (updated == null) {
            metadataUpdatedRepository.save(new MetadataUpdated(metadataType, LocalDateTime.now(), version));
        } else {
            updated.setUpdated(LocalDateTime.now());
            updated.setVersion(version);
        }
    }

    @Transactional(readOnly = true)
    public MetadataUpdated findMetadataUpdatedByMetadataType(final MetadataType metadataType) {
        return metadataUpdatedRepository.findByMetadataType(metadataType.name());
    }

    @Transactional(readOnly = true)
    public ZonedDateTime getMetadataUpdatedTime(final MetadataType metadataType) {
        final MetadataUpdated updated = metadataUpdatedRepository.findByMetadataType(metadataType.name());

        return updated == null ? null : updated.getUpdated().atZone(ZoneId.systemDefault());
    }

    @Transactional(readOnly = true)
    public MetadataVersions getCurrentMetadataVersions() {
        final MetadataVersions metadataVersions = new MetadataVersions();

        metadataVersions.addVersion(MetadataType.LOCATIONS, null, getMetadataVersion(MetadataType.LOCATIONS));
        metadataVersions.addVersion(MetadataType.LOCATION_TYPES, null, getMetadataVersion(MetadataType.LOCATION_TYPES));

        return metadataVersions;
    }

    public String getMetadataVersion(final MetadataType metadataType) {
        final MetadataUpdated updated = metadataUpdatedRepository.findByMetadataType(metadataType.name());

        return updated == null ? null : updated.getVersion();
    }

}
