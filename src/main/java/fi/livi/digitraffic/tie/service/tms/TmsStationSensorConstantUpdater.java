package fi.livi.digitraffic.tie.service.tms;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.dao.roadstation.RoadStationRepository;
import fi.livi.digitraffic.tie.dto.v1.tms.TmsSensorConstantValueDtoV1;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioArvoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioVO;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.roadstation.RoadStation;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.MetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.lotju.LotjuTmsStationMetadataClientWrapper;

@ConditionalOnNotWebApplication
@Component
public class TmsStationSensorConstantUpdater {
    private static final Logger log = LoggerFactory.getLogger(TmsStationSensorConstantUpdater.class);

    private final TmsStationSensorConstantService tmsStationSensorConstantService;
    private final TmsStationService tmsStationService;
    private final DataStatusService dataStatusService;
    private final LotjuTmsStationMetadataClientWrapper lotjuTmsStationMetadataClientWrapper;
    private final RoadStationRepository roadStationRepository;

    @Autowired
    public TmsStationSensorConstantUpdater(final TmsStationSensorConstantService tmsStationSensorConstantService,
                                           final TmsStationService tmsStationService,
                                           final DataStatusService dataStatusService,
                                           final LotjuTmsStationMetadataClientWrapper lotjuTmsStationMetadataClientWrapper,
                                           final RoadStationRepository roadStationRepository
    ) {
        this.tmsStationSensorConstantService = tmsStationSensorConstantService;
        this.tmsStationService = tmsStationService;
        this.dataStatusService = dataStatusService;
        this.lotjuTmsStationMetadataClientWrapper = lotjuTmsStationMetadataClientWrapper;
        this.roadStationRepository = roadStationRepository;
    }

    public boolean updateTmsStationsSensorConstant(final long anturilotjuId,
                                                   final MetadataUpdatedMessageDto.UpdateType updateType,
                                                   final long roadStationLotjuId) {

        final Optional<RoadStation>
                rs = roadStationRepository.findByTypeAndLotjuId(RoadStationType.TMS_STATION, roadStationLotjuId);
        final Long rsNaturalId = rs.map(RoadStation::getNaturalId).orElse(null);
        if (updateType.isDelete()) {
            if (tmsStationSensorConstantService.obsoleteSensorConstantWithLotjuId(anturilotjuId, rsNaturalId)) {
                dataStatusService.updateDataUpdated(DataType.TMS_SENSOR_CONSTANT_METADATA);
                return true;
            }
        } else {
            final LamAnturiVakioVO anturiVakio = lotjuTmsStationMetadataClientWrapper.getLamAnturiVakio(anturilotjuId);
            if (anturiVakio == null) {
                log.warn(
                        "method=updateTmsStationsSensorConstant TMS stationg sensor constant with lotjuId={} not found",
                        anturilotjuId);
            } else if (tmsStationSensorConstantService.updateSensorConstant(anturiVakio, rsNaturalId)) {
                dataStatusService.updateDataUpdated(DataType.TMS_SENSOR_CONSTANT_METADATA);
                return true;
            }
        }
        return false;
    }

    /**
     * Updates all available sensorConstants of tms road stations
     */
    public boolean updateTmsStationsSensorConstants() {
        final StopWatch start = StopWatch.createStarted();

        // Get current TmsStations
        final List<Long> tmsLotjuIds =
                tmsStationService.findAllTmsStationsLotjuIds();

        final List<LamAnturiVakioVO> allLamAnturiVakios =
                lotjuTmsStationMetadataClientWrapper.getAllLamAnturiVakios(tmsLotjuIds);

        final boolean updated = tmsStationSensorConstantService.updateSensorConstants(allLamAnturiVakios);

        if (updated) {
            dataStatusService.updateDataUpdated(DataType.TMS_SENSOR_CONSTANT_METADATA);
        }
        dataStatusService.updateDataUpdated(DataType.TMS_SENSOR_CONSTANT_METADATA_CHECK);

        log.info("method=updateTmsStationsSensorConstants tms count={} tookMs={}", tmsLotjuIds.size(),
                start.getDuration().toMillis());
        return updated;
    }

    /**
     * Updates all available sensorConstants of tms road stations
     */
    public boolean updateTmsStationSensorConstants(final long stationLotjuId) {
        final StopWatch start = StopWatch.createStarted();

        final List<LamAnturiVakioVO> allLamAnturiVakios =
                lotjuTmsStationMetadataClientWrapper.getAllLamAnturiVakios(Collections.singleton(stationLotjuId));

        final boolean updated =
                tmsStationSensorConstantService.updateSingleStationsSensorConstants(allLamAnturiVakios, stationLotjuId);

        if (updated) {
            dataStatusService.updateDataUpdated(DataType.TMS_SENSOR_CONSTANT_METADATA);
        }
        dataStatusService.updateDataUpdated(DataType.TMS_SENSOR_CONSTANT_METADATA_CHECK);

        log.info("method=updateTmsStationSensorConstants tms lotjuId={} tookMs={}", stationLotjuId,
                start.getDuration().toMillis());
        return updated;
    }

    public boolean updateTmsStationsSensorConstantValue(final long lamAnturiVakioArvoLotjuId,
                                                        final long roadStationLotjuId,
                                                        final MetadataUpdatedMessageDto.UpdateType updateType) {
        final Optional<RoadStation>
                rs = roadStationRepository.findByTypeAndLotjuId(RoadStationType.TMS_STATION, roadStationLotjuId);
        final Long rsNaturalId = rs.map(RoadStation::getNaturalId).orElse(null);
        if (updateType.isDelete()) {
            if (tmsStationSensorConstantService.updateSensorConstantValueToObsoleteWithSensorConstantValueLotjuId(
                    lamAnturiVakioArvoLotjuId, rsNaturalId)) {
                dataStatusService.updateDataUpdated(DataType.TMS_SENSOR_CONSTANT_VALUE_DATA);
                return true;
            }
        } else {
            final TmsSensorConstantValueDtoV1 constantValue =
                    tmsStationSensorConstantService.getStationSensorConstantValue(roadStationLotjuId,
                            lamAnturiVakioArvoLotjuId);
            if (constantValue != null) {
                final Long lamAnturiVakioLotjuId = constantValue.getConstantLotjuId();
                final List<LamAnturiVakioArvoVO> anturiVakioArvos =
                        lotjuTmsStationMetadataClientWrapper.getAnturiVakioArvos(lamAnturiVakioLotjuId);
                if (anturiVakioArvos.isEmpty()) {
                    log.warn(
                            "method=updateTmsStationsSensorConstantValue sensor constant value with sensorConstantValueLotjuId: {} and SensorConstant lotjuId={} not found",
                            lamAnturiVakioArvoLotjuId, lamAnturiVakioLotjuId);
                } else if (tmsStationSensorConstantService.updateSingleSensorConstantValues(anturiVakioArvos,
                        rsNaturalId)) {
                    dataStatusService.updateDataUpdated(DataType.TMS_SENSOR_CONSTANT_VALUE_DATA);
                    return true;
                }
            } else { // Value don't exist in db
                final boolean constants = updateTmsStationSensorConstants(roadStationLotjuId);
                final boolean values = updateTmsStationSensorConstantsValues(roadStationLotjuId);
                return constants || values;
            }
        }
        return false;
    }

    /**
     * Updates all available sensorConstantValues of tms road stations
     */
    public boolean updateTmsStationsSensorConstantsValues() {
        final StopWatch start = StopWatch.createStarted();

        final List<LamAnturiVakioArvoVO> allLamAnturiVakioArvos =
                lotjuTmsStationMetadataClientWrapper.getAllLamAnturiVakioArvos();

        final boolean updated = tmsStationSensorConstantService.updateSensorConstantValues(allLamAnturiVakioArvos);

        if (updated) {
            dataStatusService.updateDataUpdated(DataType.TMS_FREE_FLOW_SPEEDS_DATA);
            dataStatusService.updateDataUpdated(DataType.TMS_SENSOR_CONSTANT_VALUE_DATA);
        }
        dataStatusService.updateDataUpdated(DataType.TMS_SENSOR_CONSTANT_VALUE_DATA_CHECK);

        log.info("method=updateTmsStationsSensorConstantsValues tookMs={}", start.getDuration().toMillis());
        return updated;
    }

    public boolean updateTmsStationSensorConstantsValues(final long roadStationLotjuId) {
        final StopWatch start = StopWatch.createStarted();
        final List<LamAnturiVakioArvoVO> allLamAnturiVakioArvos =
                lotjuTmsStationMetadataClientWrapper.getAsemanLamAnturiVakioArvos(roadStationLotjuId);
        final Optional<RoadStation>
                rs = roadStationRepository.findByTypeAndLotjuId(RoadStationType.TMS_STATION, roadStationLotjuId);

        final boolean updated =
                tmsStationSensorConstantService.updateStationSensorConstantValues(allLamAnturiVakioArvos,
                        rs.map(RoadStation::getLotjuId).orElse(null));

        if (updated) {
            dataStatusService.updateDataUpdated(DataType.TMS_FREE_FLOW_SPEEDS_DATA);
            dataStatusService.updateDataUpdated(DataType.TMS_SENSOR_CONSTANT_VALUE_DATA);
        }
        dataStatusService.updateDataUpdated(DataType.TMS_SENSOR_CONSTANT_VALUE_DATA_CHECK);

        log.info("method=updateTmsStationsSensorConstantsValues tookMs={}", start.getDuration().toMillis());
        return updated;
    }
}
