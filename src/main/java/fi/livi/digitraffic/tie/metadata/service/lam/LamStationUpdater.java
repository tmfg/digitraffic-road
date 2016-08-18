package fi.livi.digitraffic.tie.metadata.service.lam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.model.LamStation;
import fi.livi.digitraffic.tie.metadata.model.LamStationType;
import fi.livi.digitraffic.tie.metadata.model.RoadDistrict;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.RoadDistrictService;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuLamStationClient;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29.LamAsemaVO;

@Service
public class LamStationUpdater extends AbstractLamStationAttributeUpdater {

    private static final Logger log = LoggerFactory.getLogger(AbstractLamStationAttributeUpdater.class);

    public static final String INSERT_FAILED = "Insert failed ";

    private final LamStationService lamStationService;
    private final RoadDistrictService roadDistrictService;
    private final StaticDataStatusService staticDataStatusService;

    private final LotjuLamStationClient lotjuLamStationClient;

    @Autowired
    public LamStationUpdater(final RoadStationService roadStationService,
                             final LamStationService lamStationService,
                             final RoadDistrictService roadDistrictService,
                             final StaticDataStatusService staticDataStatusService,
                             final LotjuLamStationClient lotjuLamStationClient) {
        super(roadStationService);
        this.lamStationService = lamStationService;
        this.roadDistrictService = roadDistrictService;
        this.staticDataStatusService = staticDataStatusService;
        this.lotjuLamStationClient = lotjuLamStationClient;
    }

    @Transactional
    public boolean updateLamStations() {
        log.info("Update Lam Stations start");

        if (lotjuLamStationClient == null) {
            log.warn("Not updating lam stations because no lotjuLamStationClient defined");
            return false;
        }

        final List<LamAsemaVO> stations = lotjuLamStationClient.getLamAsemas();

        if (log.isDebugEnabled()) {
            log.debug("Fetched LAMs:");
            for (final LamAsemaVO station : stations) {
                log.debug(ToStringBuilder.reflectionToString(station));
            }
        }

        final Map<Long, LamStation> currentStations = lamStationService.findAllLamStationsMappedByByNaturalId();

        final boolean updateStaticDataStatus = updateLamStations(stations, currentStations);
        updateStaticDataStatus(updateStaticDataStatus);
        log.info("updateLamStations end");
        return updateStaticDataStatus;
    }

    private void updateStaticDataStatus(final boolean updateStaticDataStatus) {
        staticDataStatusService.updateStaticDataStatus(StaticDataStatusService.StaticStatusType.LAM, updateStaticDataStatus);
    }

    private boolean updateLamStations(final List<LamAsemaVO> stations, final Map<Long, LamStation> currentStations) {
        final List<LamStation> obsolete = new ArrayList<>(); // naturalIds of obsolete lam-stations
        final List<Pair<LamAsemaVO, LamStation>> update = new ArrayList<>(); // lam-stations to update
        final List<LamAsemaVO> insert = new ArrayList<>(); // new lam-stations

        int invalid = 0;
        for ( final LamAsemaVO la : stations ) {

            if ( validate(la) ) {
                final Long lamNaturalId = convertToLamNaturalId(la.getVanhaId());
                final LamStation currentSaved = currentStations.remove(lamNaturalId);

                if ( currentSaved != null && CollectionStatus.isPermanentlyDeletedKeruunTila(la.getKeruunTila()) ) {
                    obsolete.add(currentSaved);
                } else if ( currentSaved != null ) {
                    update.add(Pair.of(la, currentSaved));
                } else {
                    insert.add(la);
                }
            } else {
                invalid++;
            }
        }

        if (invalid > 0) {
            log.warn("Found " + invalid + " LamAsemas from LOTJU");
        }

        // lam-stations in database, but not in server
        obsolete.addAll(currentStations.values());

        final int obsoleted = obsoleteLamStations(obsolete);
        final int uptaded = updateLamStations(update);
        final int inserted = insertLamStations(insert);

        log.info("Obsoleted " + obsoleted + " LamStations");
        log.info("Uptaded " + uptaded + " LamStations");
        log.info("Inserted " + inserted + " LamStations");
        if (insert.size() > inserted) {
            log.warn(INSERT_FAILED + "for " + (insert.size()-inserted) + " LamStations");
        }

        return obsoleted > 0 || inserted > 0;
    }

    /**
     * @param roadStationVanhaId LamAsema.vanhaId
     * @return
     */
    private static Long convertToLamNaturalId(final Integer roadStationVanhaId) {
        return roadStationVanhaId == null ? null : roadStationVanhaId - 23000L;
    }

    private int insertLamStations(final List<LamAsemaVO> insert) {

        int counter = 0;
        for (final LamAsemaVO la : insert) {
            if (insertLamStation(la)) {
                counter++;
            }
        }
        return counter;
    }

    private boolean insertLamStation(final LamAsemaVO la) {

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
            final LamStation newLamStation = new LamStation();
            newLamStation.setSummerFreeFlowSpeed1(0);
            newLamStation.setSummerFreeFlowSpeed2(0);
            newLamStation.setWinterFreeFlowSpeed1(0);
            newLamStation.setWinterFreeFlowSpeed2(0);
            final RoadStation rs = new RoadStation(RoadStationType.LAM_STATION);
            newLamStation.setRoadStation(rs);

            setRoadAddressIfNotSet(rs);

            updateLamStationAttributes(la, roadDistrict, newLamStation);

            if (rs.getRoadAddress() != null) {
                roadStationService.save(rs.getRoadAddress());
            }
            roadStationService.save(rs);
            lamStationService.save(newLamStation);
            log.info("Created new " + newLamStation);
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

    private int updateLamStations(final List<Pair<LamAsemaVO, LamStation>> update) {

        final Map<Long, RoadStation> orphansNaturalIdToRoadStationMap =
                roadStationService.findOrphansByTypeMappedByNaturalId(RoadStationType.LAM_STATION);

        int counter = 0;
        for (final Pair<LamAsemaVO, LamStation> pair : update) {

            final LamAsemaVO la = pair.getLeft();
            final LamStation ls = pair.getRight();

            final int hash = HashCodeBuilder.reflectionHashCode(ls);
            final String before = ReflectionToStringBuilder.toString(ls);

            log.debug("Updating " + ToStringHelpper.toString(la));

            RoadStation rs = ls.getRoadStation();
            if (rs == null) {
                final Integer naturalId = la.getVanhaId();

                rs = naturalId != null ? orphansNaturalIdToRoadStationMap.get(naturalId.longValue()) : null;
                if (rs == null) {
                    rs = new RoadStation(RoadStationType.LAM_STATION);
                    if (naturalId != null) {
                        orphansNaturalIdToRoadStationMap.put(naturalId.longValue(), rs);
                    }
                }
                ls.setRoadStation(rs);
            }

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
                          roadSectionNaturalId + " vs old: " + ls.getRoadStation().getRoadAddress().getRoadSection() + ", LamAsema.getTieosoite().getTienumero(): " +
                          roadNaturalId + " vs old: " + ls.getRoadStation().getRoadAddress().getRoadNumber());
                rd = ls.getRoadDistrict();
            } else {
                if (ls.getRoadDistrict().getNaturalId() != rd.getNaturalId()) {
                    log.info("Update LAM station (naturalID: " + convertToLamNaturalId(la.getVanhaId()) + ") " + la.getNimi() +
                             " road district naturalId " + ls.getRoadDistrict().getNaturalId() + " -> " + rd.getNaturalId());
                }
            }

            if ( updateLamStationAttributes(la, rd, ls) ||
                 hash != HashCodeBuilder.reflectionHashCode(ls) ) {
                counter++;
                log.info("Updated LamStation:\n" + before + " -> \n" + ReflectionToStringBuilder.toString(ls));
            }
            if (rs.getRoadAddress() != null && rs.getRoadAddress().getId() == null) {
                roadStationService.save(rs.getRoadAddress());
                log.info("Created new RoadAddress " + rs.getRoadAddress());
            }
            if (rs.getId() == null) {
                roadStationService.save(rs);
                log.info("Created new RoadStation " + ls.getRoadStation());
            }
        }
        return counter;
    }

    private static boolean updateLamStationAttributes(final LamAsemaVO from, final RoadDistrict roadDistrict, final LamStation to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);
        to.setNaturalId(convertToLamNaturalId(from.getVanhaId()));
        to.setLotjuId(from.getId());

        to.setName(from.getNimi());
        to.setDirection1Municipality(from.getSuunta1Kunta());
        to.setDirection1MunicipalityCode(from.getSuunta1KuntaKoodi());
        to.setDirection2Municipality(from.getSuunta2Kunta());
        to.setDirection2MunicipalityCode(from.getSuunta2KuntaKoodi());
        to.setLamStationType(LamStationType.convertFromLamasemaTyyppi(from.getTyyppi()));
        to.setCalculatorDeviceType(CalculatorDeviceType.convertFromLaiteTyyppi(from.getLaskinlaite()));

        to.setRoadDistrict(roadDistrict);

        // Update RoadStation
        final boolean updated = updateRoadStationAttributes(from, to.getRoadStation());
        to.setObsolete(to.getRoadStation().isObsolete());
        to.setObsoleteDate(to.getRoadStation().getObsoleteDate());

        return  updated ||
                hash != HashCodeBuilder.reflectionHashCode(to);
    }

    private static int obsoleteLamStations(final List<LamStation> obsolete) {
        int counter = 0;
        for (final LamStation station : obsolete) {
            if (station.obsolete()) {
                log.debug("Obsolete LamStation " + station.getId() + ", naturalId" + station.getNaturalId());
                counter++;
            }
        }
        return counter;
    }
}
