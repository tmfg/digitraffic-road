package fi.livi.digitraffic.tie.service.jms.marshaller.dto;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

public final class TmsMetadataUpdatedMessageDto extends MetadataUpdatedMessageDto {

    public enum EntityType {
        TMS_STATION("LAM_ASEMA"),
        TMS_SENSOR("LAM_ANTURI"),
        TMS_COMPUTATIONAL_SENSOR("LAM_LASKENNALLINENANTURI"),
        TMS_SENSOR_CONSTANT("LAM_ANTURIVAKIO"),
        TMS_SENSOR_CONSTANT_VALUE("LAM_ANTURIVAKIO_ARVO"),
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
    }

    private final EntityType entityType;

    public TmsMetadataUpdatedMessageDto(final Long lotjuId, final Set<Long> asemaLotjuIds, final UpdateType updateType, final Instant updateTime, final EntityType entityType) {
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
