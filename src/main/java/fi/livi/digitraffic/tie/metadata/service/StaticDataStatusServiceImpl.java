package fi.livi.digitraffic.tie.metadata.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.dao.MetadataUpdatedRepository;
import fi.livi.digitraffic.tie.metadata.dao.StaticDataStatusDAO;
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;

@Service
public class StaticDataStatusServiceImpl implements StaticDataStatusService {

    private final StaticDataStatusDAO staticDataStatusDAO;
    private final MetadataUpdatedRepository metadataUpdatedRepository;


    @Autowired
    public StaticDataStatusServiceImpl(final StaticDataStatusDAO staticDataStatusDAO,
                                       final MetadataUpdatedRepository metadataUpdatedRepository) {
        this.staticDataStatusDAO = staticDataStatusDAO;
        this.metadataUpdatedRepository = metadataUpdatedRepository;
    }

    @Transactional
    @Override
    public void updateStaticDataStatus(final StaticStatusType type, final boolean updateStaticDataStatus) {
        staticDataStatusDAO.updateStaticDataStatus(type, updateStaticDataStatus);
    }

    @Transactional
    @Override
    public void updateMetadataUpdated(final MetadataType metadataType) {
        MetadataUpdated updated = metadataUpdatedRepository.findByMetadataType(metadataType.name());
        if (updated == null) {
            updated = new MetadataUpdated(metadataType, LocalDateTime.now());
            metadataUpdatedRepository.save(updated);
        } else {
            updated.setUpdated(LocalDateTime.now());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public MetadataUpdated findMetadataUpdatedByMetadataType(final MetadataType metadataType) {
        return metadataUpdatedRepository.findByMetadataType(metadataType.name());
    }

}
