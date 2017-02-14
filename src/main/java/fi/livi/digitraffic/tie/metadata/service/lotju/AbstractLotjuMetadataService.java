package fi.livi.digitraffic.tie.metadata.service.lotju;

public abstract class AbstractLotjuMetadataService {

    private final boolean enabled;

    public AbstractLotjuMetadataService(final boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
