package fi.livi.digitraffic.tie.service.v1.tms;

import static fi.livi.digitraffic.tie.service.jms.marshaller.dto.MetadataUpdatedMessageDto.UpdateType.UPDATE;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.service.DataStatusService;
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

    private TmsStationUpdater tmsStationUpdater;
    private TmsSensorUpdater tmsSensorUpdater;
    private TmsStationSensorConstantUpdater tmsStationSensorConstantUpdater;
    private final DataStatusService dataStatusService;

    public TmsMetadataUpdateMessageHandler(final TmsStationUpdater tmsStationUpdater,
                                           final TmsSensorUpdater tmsSensorUpdater,
                                           final TmsStationSensorConstantUpdater tmsStationSensorConstantUpdater,
                                           final DataStatusService dataStatusService) {
        this.tmsStationUpdater = tmsStationUpdater;
        this.tmsSensorUpdater = tmsSensorUpdater;
        this.tmsStationSensorConstantUpdater = tmsStationSensorConstantUpdater;
        this.dataStatusService = dataStatusService;
    }

    // Disable info logging as it can be normally over 1 s. Log only if over default warning level 5 s.
    @PerformanceMonitor(maxInfoExcecutionTime = 100000)
    public int updateTmsMetadataFromJms(final List<TmsMetadataUpdatedMessageDto> tmsMetadataUpdates) {
        int updateCount = 0;

        for (TmsMetadataUpdatedMessageDto u : tmsMetadataUpdates) {
            log.debug("method=updateTmsMetadataFromJms {}", ToStringHelper.toStringFull(u));
            final EntityType type = u.getEntityType();
            final UpdateType updateType = u.getUpdateType();

            if ( u.getUpdateTime().plus(1, ChronoUnit.DAYS).isAfter(Instant.now()) ) {
                switch (type) {
                case TMS_STATION:
                    if ( tmsStationUpdater.updateTmsStationAndSensors(u.getLotjuId(), updateType) ) {
                        updateCount++;
                    }
                    break;
                case TMS_COMPUTATIONAL_SENSOR:
                    // Contains also id's of the stations affected
                    if ( tmsSensorUpdater.updateTmsSensor(u.getLotjuId(), updateType) ) {
                        updateCount++;
                    }
                    // Even when updateType would be delete, this means always update for station
                    updateCount += u.getAsemmaLotjuIds().stream()
                        .filter(asemaId -> tmsStationUpdater.updateTmsStationAndSensors(asemaId, UPDATE)).count();
                    break;
                case TMS_SENSOR_CONSTANT:
                    if ( tmsStationSensorConstantUpdater.updateTmsStationsSensorConstant(u.getLotjuId(), updateType) ) {
                        updateCount++;
                    }
                    break;
                case TMS_SENSOR_CONSTANT_VALUE:
                    if ( tmsStationSensorConstantUpdater.updateTmsStationsSensorConstantValue(u.getLotjuId(), updateType) ) {
                        updateCount++;
                    }
                    break;
                case ROAD_ADDRESS:
                    // Always update
                    updateCount += u.getAsemmaLotjuIds().stream()
                        .filter(asemaId -> tmsStationUpdater.updateTmsStationAndSensors(asemaId, UPDATE)).count();
                    break;
                case TMS_SENSOR:
                    // no handle as TMS_SENSOR update won't affect us.
                    // Only computational sensors matters
                    break;
                default:
                    throw new IllegalArgumentException("Unknown EntityType " + type);
                }
            }
        }

        return updateCount;
    }
}
