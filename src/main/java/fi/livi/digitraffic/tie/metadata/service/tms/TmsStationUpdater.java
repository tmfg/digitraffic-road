package fi.livi.digitraffic.tie.metadata.service.tms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.metadata.model.CalculatorDeviceType;
import fi.livi.digitraffic.tie.metadata.model.RoadDistrict;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.TmsStation;
import fi.livi.digitraffic.tie.metadata.model.TmsStationType;
import fi.livi.digitraffic.tie.metadata.service.RoadDistrictService;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuTmsStationClient;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.LamAsemaVO;

@Service
public class TmsStationUpdater extends AbstractTmsStationAttributeUpdater {

    private static final Logger log = LoggerFactory.getLogger(AbstractTmsStationAttributeUpdater.class);

    public static final String INSERT_FAILED = "Insert failed ";

    private final TmsStationService tmsStationService;
    private final RoadDistrictService roadDistrictService;
    private final StaticDataStatusService staticDataStatusService;

    private final LotjuTmsStationClient lotjuTmsStationClient;

    @Autowired
    public TmsStationUpdater(final RoadStationService roadStationService,
                             final TmsStationService tmsStationService,
                             final RoadDistrictService roadDistrictService,
                             final StaticDataStatusService staticDataStatusService,
                             final LotjuTmsStationClient lotjuTmsStationClient) {
        super(roadStationService);
        this.tmsStationService = tmsStationService;
        this.roadDistrictService = roadDistrictService;
        this.staticDataStatusService = staticDataStatusService;
        this.lotjuTmsStationClient = lotjuTmsStationClient;
    }

    @Transactional
    public boolean updateTmsStations() {
        log.info("Update tms Stations start");

        if (lotjuTmsStationClient == null) {
            log.warn("Not updating tms stations because lotjuTmsStationClient not defined");
            return false;
        }

        final List<LamAsemaVO> asemas = lotjuTmsStationClient.getLamAsemas();

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
        asemas.stream().forEach(la -> {
            if (validate(la)) {
                final Long tmsNaturalId = convertToTmsNaturalId(la.getVanhaId());
                final TmsStation currentSaved = noLotjuIds.remove(tmsNaturalId);

                if (currentSaved != null) {
                    currentSaved.setLotjuId(la.getId());
                    currentSaved.getRoadStation().setLotjuId(la.getId());
                    updated.addAndGet(1);
                }
            }
        });

        // Obsolete not found stations
        final AtomicInteger obsoleted = new AtomicInteger();
        noLotjuIds.values().stream().forEach(tms -> {
            if (tms.obsolete()) {
                obsoleted.addAndGet(1);
            }
        });

        log.info("Obsoleted {} TmsStations", obsoleted);
        log.info("Fixed {} TmsStations without lotjuId", updated);

        return obsoleted.get() > 0 || updated.get() > 0;
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
        AtomicInteger obsoleted = new AtomicInteger();
        currentStationsByLotjuId.values().stream().forEach(tms -> {
            if (tms.obsolete()) {
                obsoleted.addAndGet(1);
            }
        });

        final int updated = updateTmsStations(update);
        final int inserted = insertTmsStations(insert);

        log.info("Obsoleted {} TmsStations", obsoleted);
        log.info("Updated {} TmsStations", updated);
        log.info("Inserted " + inserted + " TmsStations");
        if (insert.size() > inserted) {
            log.warn(INSERT_FAILED + "for " + (insert.size()-inserted) + " TmsStations");
        }

        return obsoleted.get() > 0 || inserted > 0;
    }

    /**
     * @param roadStationVanhaId LamAsema.vanhaId
     * @return
     */
    private static Long convertToTmsNaturalId(final Integer roadStationVanhaId) {
        return roadStationVanhaId == null ? null : roadStationVanhaId - 23000L;
    }

    private int insertTmsStations(final List<LamAsemaVO> insert) {

        int counter = 0;
        for (final LamAsemaVO la : insert) {
            if (insertTmsStation(la)) {
                counter++;
            }
        }
        return counter;
    }

    private boolean insertTmsStation(final LamAsemaVO la) {

        final Integer roadNaturalId = la.getTieosoite().getTienumero();
        final Integer roadSectionNaturalId = la.getTieosoite().getTieosa();

        if (roadNaturalId == null ) {
            log.error(INSERT_FAILED + ToStringHelpper.toString(la) + ": LamAsema.getTieosoite().getTienumero() is null");
            return false;
        }
        if (roadSectionNaturalId == null ) {
            log.error(INSERT_FAILED + ToStringHelpper.toString(la) + ": LamAsema.getTieosoite().getTieosa() is null");
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
            log.error(
                    INSERT_FAILED + ToStringHelpper.toString(la) + ": Could not find RoadDistrict with roadSectionNaturalId " + roadSectionNaturalId + ", roadNaturalId: " + roadNaturalId);
            return false;
        }
    }

    private static boolean validate(final LamAsemaVO la) {
        final boolean valid = la.getVanhaId() != null;
        if (!valid) {
            log.error(ToStringHelpper.toString(la) + " is invalid: has null vanhaId");
        }
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
                log.error(ToStringHelpper.toString(la) + " update failed: LamAsema.getTieosoite().getTienumero() is null");
            }
            if ( roadSectionNaturalId == null ) {
                log.error(ToStringHelpper.toString(la) + " update failed: LamAsema.getTieosoite().getTieosa() is null");
            }

            RoadDistrict rd = (roadNaturalId != null && roadSectionNaturalId != null) ?
                    roadDistrictService.findByRoadSectionAndRoadNaturalId(roadSectionNaturalId, roadNaturalId) : null;
            if (rd == null) {
                log.error(ToStringHelpper.toString(la) + " update: Could not find RoadDistrict with LamAsema.getTieosoite().getTieosa() " +
                          roadSectionNaturalId + " vs old: " + tms.getRoadStation().getRoadAddress().getRoadSection() + ", LamAsema.getTieosoite().getTienumero(): " +
                          roadNaturalId + " vs old: " + tms.getRoadStation().getRoadAddress().getRoadNumber());
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
                log.info("Updated TmsStation:\n" + before + " -> \n" + ReflectionToStringBuilder.toString(tms));
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

    private int obsoleteTmsStations(final List<Pair<LamAsemaVO, TmsStation>> obsolete) {
        int counter = 0;
        for (final Pair<LamAsemaVO, TmsStation> tmsPair : obsolete) {
            if (tmsPair.getValue().obsolete()) {
                log.debug("Obsolete TmsStation " + tmsPair.getValue());
                counter++;
            }
        }
        // Update also obsolete stations attributes
        updateTmsStations(obsolete.stream().filter(o -> o.getLeft() != null).collect(Collectors.toList()));

        return counter;
    }
}
