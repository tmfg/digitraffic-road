package fi.livi.digitraffic.tie.service.jms.marshaller.dto;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

public class CameraMetadataUpdatedMessageDto extends MetadataUpdatedMessageDto {

    public enum EntityType {
        CAMERA("KAMERA"),
        VIDEO_SERVER("VIDEOPALVELIN"),
        CAMERA_CONFIGURATION("KAMERAKOKOONPANO"),
        PRESET("ESIASENTO"),
        MASTER_STORAGE("MASTER_TIETOVARASTO"),
        ROAD_ADDRESS("TIEOSOITE");

        private final String externalValue;

        EntityType(final String externalValue) {
            this.externalValue = externalValue;
        }

        public static EntityType fromExternalValue(final String value) {
            final Optional<EntityType> found = Arrays.stream(values()).filter(v -> v.externalValue.equals(value)).findFirst();
            if (found.isPresent()) {
                return found.get();
            }
            throw new IllegalArgumentException("Unknown EntityType " + value);
        }

        public String getExternalValue() {
            return externalValue;
        }
    }

    private final EntityType entityType;

    public CameraMetadataUpdatedMessageDto(final Long lotjuId, final Set<Long> asemaLotjuIds, final UpdateType updateType, final Instant updateTime, final EntityType entityType) {
        super(lotjuId, asemaLotjuIds, updateType, updateTime);
        this.entityType = entityType;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}
