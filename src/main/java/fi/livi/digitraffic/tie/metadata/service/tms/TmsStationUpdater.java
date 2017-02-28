package fi.livi.digitraffic.tie.metadata.service.tms;

import static fi.livi.digitraffic.tie.metadata.model.CollectionStatus.isPermanentlyDeletedKeruunTila;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.metadata.model.CalculatorDeviceType;
import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.model.RoadDistrict;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.TmsStation;
import fi.livi.digitraffic.tie.metadata.model.TmsStationType;
import fi.livi.digitraffic.tie.metadata.service.RoadDistrictService;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuTmsStationMetadataService;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.LamAsemaVO;

@Service
public class TmsStationUpdater extends AbstractTmsStationAttributeUpdater {

    private final TmsStationService tmsStationService;
    private final RoadDistrictService roadDistrictService;
    private final StaticDataStatusService staticDataStatusService;
    private final LotjuTmsStationMetadataService lotjuTmsStationMetadataService;

    @Autowired
    public TmsStationUpdater(final RoadStationService roadStationService,
                             final TmsStationService tmsStationService,
                             final RoadDistrictService roadDistrictService,
                             final StaticDataStatusService staticDataStatusService,
                             final LotjuTmsStationMetadataService lotjuTmsStationMetadataService) {
        super(roadStationService, LoggerFactory.getLogger(AbstractTmsStationAttributeUpdater.class));
        this.tmsStationService = tmsStationService;
        this.roadDistrictService = roadDistrictService;
        this.staticDataStatusService = staticDataStatusService;
        this.lotjuTmsStationMetadataService = lotjuTmsStationMetadataService;
    }

    @Transactional
    public boolean updateTmsStations() {
        log.info("Update tms Stations start");

        if (!lotjuTmsStationMetadataService.isEnabled()) {
            log.warn("Not updating tms stations because LotjuTmsStationMetadataService not enabled");
            return false;
        }

        final List<LamAsemaVO> asemas = lotjuTmsStationMetadataService.getLamAsemas();

        if (log.isDebugEnabled()) {
            log.debug("Fetched LAMs:");
            for (final LamAsemaVO asema : asemas) {
                log.debug(ToStringBuilder.reflectionToString(asema));
            }
        }

        final boolean updatedWithoutLotjuIds = fixTmsStationsWithoutLotjuId(asemas);

        final Map<Long, TmsStation> currentStationsByLotjuId = tmsStationService.findAllTmsStationsMappedByByLotjuId();

        final boolean updatedTmsStations = updateTmsStationsMetadata(asemas, currentStationsByLotjuId);
        updateStaticDataStatus(updatedWithoutLotjuIds || updatedTmsStations);
        log.info("UpdateTmsStations end");
        return updatedTmsStations;
    }

    private void updateStaticDataStatus(final boolean updateStaticDataStatus) {
        staticDataStatusService.updateStaticDataStatus(StaticDataStatusService.StaticStatusType.TMS, updateStaticDataStatus);
    }

    private boolean fixTmsStationsWithoutLotjuId(final List<LamAsemaVO> asemas) {

        Map<Long, TmsStation> noLotjuIds = tmsStationService.findAllTmsStationsWithoutLotjuIdMappedByTmsNaturalId();

        final AtomicInteger updated = new AtomicInteger();
        asemas.stream().filter(la -> validate(la)).forEach(la -> {
            final Long tmsNaturalId = convertToTmsNaturalId(la.getVanhaId());
            final TmsStation currentSaved = noLotjuIds.remove(tmsNaturalId);
            if (currentSaved != null) {
                currentSaved.setLotjuId(la.getId());
                currentSaved.getRoadStation().setLotjuId(la.getId());
                updated.addAndGet(1);
            }
        });

        // Obsolete not found stations
        final long obsoleted = noLotjuIds.values().stream().filter(tms -> tms.obsolete()).count();

        log.info("Obsoleted {} TmsStations", obsoleted);
        log.info("Fixed {} TmsStations without lotjuId", updated);

        return obsoleted > 0 || updated.get() > 0;
    }

    private boolean updateTmsStationsMetadata(final List<LamAsemaVO> asemas, final Map<Long, TmsStation> currentStationsByLotjuId) {
        final List<Pair<LamAsemaVO, TmsStation>> update = new ArrayList<>(); // tms-stations to update
        final List<LamAsemaVO> insert = new ArrayList<>(); // new tms-stations

        AtomicInteger invalid = new AtomicInteger();
        asemas.stream().forEach(la -> {
            if ( validate(la) ) {
                final TmsStation currentSaved = currentStationsByLotjuId.remove(la.getId());

                if ( currentSaved != null ) {
                    update.add(Pair.of(la, currentSaved));
                } else {
                    insert.add(la);
                }
            } else {
                invalid.addAndGet(1);
            }
        });

        if (invalid.get() > 0) {
            log.warn("Found {} LamAsemas from LOTJU", invalid);
        }

        // tms-stations in database, but not in server -> obsolete
        long obsoleted = currentStationsByLotjuId.values().stream().filter(tms -> tms.obsolete()).count();

        final int updated = updateTmsStations(update);
        final long inserted = insertTmsStations(insert);

        log.info("Obsoleted {} TmsStations", obsoleted);
        log.info("Updated {} TmsStations", updated);
        log.info("Inserted {} TmsStations", inserted);
        if (insert.size() > inserted) {
            log.warn("Insert failed for {} TmsStations", (insert.size()-inserted));
        }

        return obsoleted > 0 || inserted > 0;
    }

    /**
     * @param roadStationVanhaId LamAsema.vanhaId
     * @return
     */
    private static Long convertToTmsNaturalId(final Integer roadStationVanhaId) {
        return roadStationVanhaId == null ? null : roadStationVanhaId - 23000L;
    }

    private long insertTmsStations(final List<LamAsemaVO> insert) {
        return insert.stream().filter(la -> insertTmsStation(la)).count();
    }

    private boolean insertTmsStation(final LamAsemaVO la) {

        final Integer roadNaturalId = la.getTieosoite().getTienumero();
        final Integer roadSectionNaturalId = la.getTieosoite().getTieosa();

        if (roadNaturalId == null) {
            logErrorIf(!isPermanentlyDeletedKeruunTila(la.getKeruunTila()),
                      "Insert failed {}: LamAsema.getTieosoite().getTienumero() is null",
                      ToStringHelpper.toString(la));
            return false;
        }
        if (roadSectionNaturalId == null ) {
            logErrorIf(!isPermanentlyDeletedKeruunTila(la.getKeruunTila()),
                       "Insert failed {}: LamAsema.getTieosoite().getTieosa() is null",
                       ToStringHelpper.toString(la));
            return false;
        }

        final RoadDistrict roadDistrict = roadDistrictService.findByRoadSectionAndRoadNaturalId(roadSectionNaturalId, roadNaturalId);
        if (roadDistrict != null) {
            final TmsStation newTmsStation = new TmsStation();
            newTmsStation.setSummerFreeFlowSpeed1(0);
            newTmsStation.setSummerFreeFlowSpeed2(0);
            newTmsStation.setWinterFreeFlowSpeed1(0);
            newTmsStation.setWinterFreeFlowSpeed2(0);
            final RoadStation rs = new RoadStation(RoadStationType.TMS_STATION);
            newTmsStation.setRoadStation(rs);

            setRoadAddressIfNotSet(rs);

            updateTmsStationAttributes(la, roadDistrict, newTmsStation);

            if (rs.getRoadAddress().getId() == null) {
                roadStationService.save(rs.getRoadAddress());
                log.info("Created new RoadAddress " + rs.getRoadAddress());
            }
            roadStationService.save(rs);
            tmsStationService.save(newTmsStation);
            log.info("Created new " + newTmsStation);
            return true;
        } else {
            logErrorIf(!isPermanentlyDeletedKeruunTila(la.getKeruunTila()),
                       "Insert failed {}: Could not find RoadDistrict with roadSectionNaturalId: {}, roadNaturalId: {}",
                       ToStringHelpper.toString(la), roadSectionNaturalId, roadNaturalId);
            return false;
        }
    }

    private boolean validate(final LamAsemaVO la) {
        final boolean valid = la.getVanhaId() != null;
        logErrorIf(!valid && !isPermanentlyDeletedKeruunTila(la.getKeruunTila()),
                   "{} is invalid: has null vanhaId",
                   ToStringHelpper.toString(la));
        return valid;
    }

    private int updateTmsStations(final List<Pair<LamAsemaVO, TmsStation>> update) {

        final Map<Long, RoadStation> orphansNaturalIdToRoadStationMap =
                roadStationService.findOrphansByTypeMappedByNaturalId(RoadStationType.TMS_STATION);

        final AtomicInteger counter = new AtomicInteger();
        update.stream().forEach(pair -> {

            final LamAsemaVO la = pair.getLeft();
            final TmsStation tms = pair.getRight();

            final int hash = HashCodeBuilder.reflectionHashCode(tms);
            final String before = ReflectionToStringBuilder.toString(tms);

            log.debug("Updating " + ToStringHelpper.toString(la));

            setRoadStationIfNotSet(tms, (long)la.getVanhaId(), orphansNaturalIdToRoadStationMap);

            RoadStation rs = tms.getRoadStation();
            setRoadAddressIfNotSet(rs);

            final Integer roadNaturalId = la.getTieosoite() != null ? la.getTieosoite().getTienumero() : null;
            final Integer roadSectionNaturalId = la.getTieosoite() != null ? la.getTieosoite().getTieosa() : null;

            if ( roadNaturalId == null ) {
                logErrorIf(!CollectionStatus.isPermanentlyDeletedKeruunTila(la.getKeruunTila()),
                           "{} update failed: LamAsema.getTieosoite().getTienumero() is null",
                            ToStringHelpper.toString(la));
            }
            if ( roadSectionNaturalId == null ) {
                logErrorIf(!CollectionStatus.isPermanentlyDeletedKeruunTila(la.getKeruunTila()),
                           "{} update failed: LamAsema.getTieosoite().getTieosa() is null",
                           ToStringHelpper.toString(la));
            }

            RoadDistrict rd = (roadNaturalId != null && roadSectionNaturalId != null) ?
                    roadDistrictService.findByRoadSectionAndRoadNaturalId(roadSectionNaturalId, roadNaturalId) : null;
            if (rd == null) {
                logErrorIf(!CollectionStatus.isPermanentlyDeletedKeruunTila(la.getKeruunTila()),
                          "{} update: Could not find RoadDistrict with LamAsema.getTieosoite().getTieosa() {}, LamAsema.getTieosoite().getTienumero() {}",
                           ToStringHelpper.toString(la),
                           roadSectionNaturalId,
                           roadNaturalId);
                rd = tms.getRoadDistrict();
            } else {
                if (tms.getRoadDistrict().getNaturalId() != rd.getNaturalId()) {
                    log.info("Update TMS station (naturalID: " + convertToTmsNaturalId(la.getVanhaId()) + ") " + la.getNimi() +
                             " road district naturalId " + tms.getRoadDistrict().getNaturalId() + " -> " + rd.getNaturalId());
                }
            }

            if ( updateTmsStationAttributes(la, rd, tms) ||
                 hash != HashCodeBuilder.reflectionHashCode(tms) ) {
                counter.addAndGet(1);
                log.info("Updated TmsStation:\n{} ->\n{}", before, ReflectionToStringBuilder.toString(tms));
            }
            if (rs.getRoadAddress().getId() == null) {
                roadStationService.save(rs.getRoadAddress());
                log.info("Created new RoadAddress " + rs.getRoadAddress());
            }
            if (rs.getId() == null) {
                roadStationService.save(rs);
                log.info("Created new RoadStation " + tms.getRoadStation());
            }
        });
        return counter.get();
    }

    private static void setRoadStationIfNotSet(TmsStation rws, Long tsaVanhaId, Map<Long, RoadStation> orphansNaturalIdToRoadStationMap) {
        RoadStation rs = rws.getRoadStation();

        if (rs == null) {
            rs = tsaVanhaId != null ? orphansNaturalIdToRoadStationMap.remove(tsaVanhaId) : null;
            if (rs == null) {
                rs = new RoadStation(RoadStationType.TMS_STATION);
            }
            rws.setRoadStation(rs);
        }
    }

    private static boolean updateTmsStationAttributes(final LamAsemaVO from, final RoadDistrict roadDistrict, final TmsStation to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);
        to.setNaturalId(convertToTmsNaturalId(from.getVanhaId()));
        to.setLotjuId(from.getId());

        to.setName(from.getNimi());
        to.setDirection1Municipality(from.getSuunta1Kunta());
        to.setDirection1MunicipalityCode(from.getSuunta1KuntaKoodi());
        to.setDirection2Municipality(from.getSuunta2Kunta());
        to.setDirection2MunicipalityCode(from.getSuunta2KuntaKoodi());
        to.setTmsStationType(TmsStationType.convertFromLamasemaTyyppi(from.getTyyppi()));
        to.setCalculatorDeviceType(CalculatorDeviceType.convertFromLaiteTyyppi(from.getLaskinlaite()));

        to.setRoadDistrict(roadDistrict);

        // Update RoadStation
        final boolean updated = updateRoadStationAttributes(from, to.getRoadStation());
        to.setObsolete(to.getRoadStation().isObsolete());
        to.setObsoleteDate(to.getRoadStation().getObsoleteDate());

        return  updated ||
                hash != HashCodeBuilder.reflectionHashCode(to);
    }
}
