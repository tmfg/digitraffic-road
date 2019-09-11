package fi.livi.digitraffic.tie.metadata.service.camera;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.metadata.dao.CameraPresetHistoryRepository;
import fi.livi.digitraffic.tie.metadata.model.CameraPresetHistory;

@Service
public class CameraPresetHistoryService {

    private CameraPresetHistoryRepository cameraPresetHistoryRepository;

    @Autowired
    public CameraPresetHistoryService(final CameraPresetHistoryRepository cameraPresetHistoryRepository) {
        this.cameraPresetHistoryRepository = cameraPresetHistoryRepository;
    }

    public void saveHistory(final CameraPresetHistory history) {
        cameraPresetHistoryRepository.save(history);
    }

    public CameraPresetHistory findHistory(final String presetId, final String versionId) {
        return cameraPresetHistoryRepository.findByIdPresetIdAndIdVersionId(presetId, versionId).orElse(null);
    }

    public CameraPresetHistory findLatestWithPresetId(final String presetId) {
        return cameraPresetHistoryRepository.findLatestByPresetId(presetId).orElse(null);
    }

    /** Orderer from oldest to newest */
    public List<CameraPresetHistory> findAllByPresetId(final String presetId) {
        return cameraPresetHistoryRepository.findByIdPresetIdOrderByLastModifiedAsc(presetId);
    }
}
