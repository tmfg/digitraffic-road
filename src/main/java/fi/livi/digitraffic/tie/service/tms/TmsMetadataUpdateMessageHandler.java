package fi.livi.digitraffic.tie.service.tms;

import static fi.livi.digitraffic.tie.service.jms.marshaller.dto.MetadataUpdatedMessageDto.UpdateType.UPDATE;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.MetadataUpdatedMessageDto.UpdateType;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.TmsMetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.TmsMetadataUpdatedMessageDto.EntityType;

/**
 * Service to handle JMS metadata updated messages
 */
@ConditionalOnNotWebApplication
@Component
public class TmsMetadataUpdateMessageHandler {
    private static final Logger log = LoggerFactory.getLogger(TmsMetadataUpdateMessageHandler.class);

    private final TmsStationUpdater tmsStationUpdater;
    private final TmsSensorUpdater tmsSensorUpdater;
    private final TmsStationSensorConstantUpdater tmsStationSensorConstantUpdater;

    public TmsMetadataUpdateMessageHandler(final TmsStationUpdater tmsStationUpdater,
                                           final TmsSensorUpdater tmsSensorUpdater,
                                           final TmsStationSensorConstantUpdater tmsStationSensorConstantUpdater) {
        this.tmsStationUpdater = tmsStationUpdater;
        this.tmsSensorUpdater = tmsSensorUpdater;
        this.tmsStationSensorConstantUpdater = tmsStationSensorConstantUpdater;
    }

    // Disable info logging as it can be normally over 1 s. Log only if over default warning level 5 s.
    @PerformanceMonitor(maxInfoExcecutionTime = 100000)
    public int updateMetadataFromJms(final List<TmsMetadataUpdatedMessageDto> tmsMetadataUpdates) {
        int updateCount = 0;

        for (TmsMetadataUpdatedMessageDto message : tmsMetadataUpdates) {
            log.info("method=updateMetadataFromJms roadStationType={} data: {}", RoadStationType.TMS_STATION.name(), ToStringHelper.toStringFull(message));
            final EntityType type = message.getEntityType();
            final UpdateType updateType = message.getUpdateType();

            // Skip messages that are older than 24 hours as metadata update job is running every 12 hours
            // so this could also be 12 h but for safety margin lets keep it in 24h. This could happen in case
            // of JMS connection problems for over 24 hours.
            if ( message.getUpdateTime().plus(1, ChronoUnit.DAYS).isAfter(Instant.now()) ) {
                try {
                    switch (type) {
                    case TMS_STATION:
                        updateCount += updateStations(message.getAsemaLotjuIds(), message.getUpdateType());
                        break;
                    case ROAD_ADDRESS: // We don't update specific addresses but stations using them
                        updateCount += updateStations(message.getAsemaLotjuIds());
                        break;
                    case TMS_COMPUTATIONAL_SENSOR:
                        // Contains also id's of the stations affected
                        if (tmsSensorUpdater.updateTmsSensor(message.getLotjuId(), updateType)) {
                            updateCount++;
                        }
                        // Even when updateType would be to delete, this means we have to update the affected stations
                        updateCount += updateStations(message.getAsemaLotjuIds());
                        break;
                    case TMS_SENSOR_CONSTANT:
                        if (tmsStationSensorConstantUpdater.updateTmsStationsSensorConstant(message.getLotjuId(), updateType)) {
                            updateCount++;
                        }
                        break;
                    case TMS_SENSOR_CONSTANT_VALUE:
                        if (tmsStationSensorConstantUpdater.updateTmsStationsSensorConstantValue(message.getLotjuId(), updateType)) {
                            updateCount++;
                        }
                        break;
                    case TMS_SENSOR:
                        // no handle as TMS_SENSOR update won't affect us.
                        // Only computational sensors matters
                        break;
                    default:
                        log.error(String.format("method=updateTmsMetadataFromJms Unknown EntityType %s", type));
                    }
                } catch (final Exception e) {
                    log.error(String.format("method=updateTmsMetadataFromJms Error with %s", ToStringHelper.toStringFull(message)), e);
                }
            }
        }

        return updateCount;
    }

    private int updateStations(final Set<Long> asemaLotjuIds) {
        return updateStations(asemaLotjuIds, UPDATE);
    }
    private int updateStations(final Set<Long> asemaLotjuIds, final UpdateType updateType) {
        return (int) asemaLotjuIds.stream().filter(lotjuId -> tmsStationUpdater.updateTmsStationAndSensors(lotjuId, updateType)).count();
    }
}
