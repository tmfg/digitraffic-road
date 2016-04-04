package fi.livi.digitraffic.tie.metadata.service;


import org.springframework.transaction.annotation.Transactional;

public interface StaticDataStatusService {

    enum StaticStatusType {
        LAM("LAM_DATA_LAST_UPDATED"),
        ROAD_WEATHER("RWS_DATA_LAST_UPDATED"),
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

    @Transactional
    void updateStaticDataStatus(StaticStatusType type, boolean updateStaticDataStatus);
}
