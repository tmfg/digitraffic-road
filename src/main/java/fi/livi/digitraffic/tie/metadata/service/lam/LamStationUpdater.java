package fi.livi.digitraffic.tie.metadata.service.lam;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
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
import fi.livi.digitraffic.tie.wsdl.lam.KeruunTILA;
import fi.livi.digitraffic.tie.wsdl.lam.LamAsema;

@Service
public class LamStationUpdater {
    private static final Logger log = Logger.getLogger(LamStationUpdater.class);
    public static final String INSERT_FAILED = "Insert failed: ";

    private final RoadStationService roadStationService;
    private final LamStationService lamStationService;
    private final RoadDistrictService roadDistrictService;
    private final StaticDataStatusService staticDataStatusService;

    private final LotjuLamStationClient lotjuLamStationClient;

    private static final EnumSet<KeruunTILA> POISTETUT = EnumSet.of(KeruunTILA.POISTETTU_PYSYVASTI, KeruunTILA.POISTETTU_TILAPAISESTI);

    @Autowired
    public LamStationUpdater(final RoadStationService roadStationService,
                             final LamStationService lamStationService,
                             final RoadDistrictService roadDistrictService,
                             final StaticDataStatusService staticDataStatusService,
                             final LotjuLamStationClient lotjuLamStationClient) {
        this.roadStationService = roadStationService;
        this.lamStationService = lamStationService;
        this.roadDistrictService = roadDistrictService;
        this.staticDataStatusService = staticDataStatusService;
        this.lotjuLamStationClient = lotjuLamStationClient;
    }

    @Transactional
    public void updateLamStations() {
        log.info("Update Lam Stations start");

        if (lotjuLamStationClient == null) {
            log.warn("Not updating lam stations because no lotjuLamStationClient defined");
            return;
        }

        final List<LamAsema> stations = lotjuLamStationClient.getLamAsemas();

        if (log.isDebugEnabled()) {
            log.debug("Fetched LAMs:");
            for (final LamAsema station : stations) {
                log.debug(ToStringBuilder.reflectionToString(station));
            }
        }

        final Map<Long, LamStation> currentStations = lamStationService.findAllLamStationsMappedByByNaturalId();

        final boolean updateStaticDataStatus = updateLamStations(stations, currentStations);
        updateStaticDataStatus(updateStaticDataStatus);
        log.info("updateLamStations end");
    }

    private void updateStaticDataStatus(final boolean updateStaticDataStatus) {
        staticDataStatusService.updateStaticDataStatus(StaticDataStatusService.StaticStatusType.LAM, updateStaticDataStatus);
    }

    private boolean updateLamStations(final List<LamAsema> stations, final Map<Long, LamStation> currentStations) {
        final List<LamStation> obsolete = new ArrayList<>(); // naturalIds of obsolete lam-stations
        final List<Pair<LamAsema, LamStation>> update = new ArrayList<>(); // lam-stations to update
        final List<LamAsema> insert = new ArrayList<>(); // new lam-stations

        int invalid = 0;
        for (final LamAsema la : stations) {

            if (validate(la)) {
                final Long lamNaturalId = convertToLamNaturalId(la.getVanhaId());


                final LamStation currentSaved = currentStations.remove(lamNaturalId);

                if (currentSaved != null && POISTETUT.contains(la.getKeruunTila())) {
                    obsolete.add(currentSaved);
                } else if (currentSaved != null) {
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
        log.info("Obsoleted " + obsoleted + " LamStations");

        final int uptaded = updateLamStations(update);
        log.info("Uptaded " + uptaded + " LamStations");

        final int inserted = insertLamStations(insert);
        log.info("Inserted " + inserted + " LamStations");
        if (insert.size() > inserted) {
            log.warn(INSERT_FAILED + " for " + (insert.size()-inserted) + " LamStations");
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

    private int insertLamStations(final List<LamAsema> insert) {
        int counter = 0;
        for (final LamAsema la : insert) {
            if (insertLamStation(la)) {
                counter++;
            }
        }
        return counter;
    }

    private boolean insertLamStation(final LamAsema la) {

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
            final RoadStation newRoadStation = new RoadStation();
            newLamStation.setRoadStation(newRoadStation);
            updateLamStationAttributes(la, roadDistrict, newLamStation);

            roadStationService.save(newRoadStation);
            lamStationService.save(newLamStation);
            log.info("Created new " + newLamStation);
            return true;
        } else {
            log.error(
                    INSERT_FAILED + ToStringHelpper.toString(la) + ": Could not find RoadDistrict with roadSectionNaturalId " + roadSectionNaturalId + ", roadNaturalId: " + roadNaturalId);
            return false;
        }
    }

    private static boolean validate(final LamAsema la) {
        final boolean valid = la.getVanhaId() != null;
        if (!valid) {
            log.error(ToStringHelpper.toString(la) +" is invalid: has null vanhaId");
        }
        return valid;
    }

    private int updateLamStations(final List<Pair<LamAsema, LamStation>> update) {

        int counter = 0;
        for (final Pair<LamAsema, LamStation> pair : update) {

            final LamAsema la = pair.getLeft();
            final LamStation ls = pair.getRight();

            log.debug("Updating " + ToStringHelpper.toString(la));

            final Integer roadNaturalId = la.getTieosoite().getTienumero();
            final Integer roadSectionNaturalId = la.getTieosoite().getTieosa();

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
                          roadSectionNaturalId + " vs old: " + ls.getRoadStation().getRoadPart() + ", LamAsema.getTieosoite().getTienumero(): " +
                          roadNaturalId + " vs old: " + ls.getRoadStation().getRoadNumber());
                rd = ls.getRoadDistrict();
            } else {
                if (ls.getRoadDistrict().getNaturalId() != rd.getNaturalId()) {
                    log.info("Update LAM station (naturalID: " + convertToLamNaturalId(la.getVanhaId()) + ") " + la.getNimi() +
                             " road district naturalId " + ls.getRoadDistrict().getNaturalId() + " -> " + rd.getNaturalId());
                }
            }

            if ( updateLamStationAttributes(la, rd, ls) ) {
                counter++;
            }
        }
        return counter;
    }

    private static boolean updateLamStationAttributes(final LamAsema from, final RoadDistrict roadDistrict, final LamStation to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);
        to.setNaturalId(convertToLamNaturalId(from.getVanhaId()));
        to.setLotjuId(from.getId());

        to.setName(from.getNimi());
        to.setDirection1Municipality(from.getSuunta1Kunta());
        to.setDirection1MunicipalityCode(from.getSuunta1KuntaKoodi());
        to.setDirection2Municipality(from.getSuunta2Kunta());
        to.setDirection2MunicipalityCode(from.getSuunta2KuntaKoodi());
        to.setLamStationType(LamStationType.convertFromKameraTyyppi(from.getTyyppi()));
        to.setRoadDistrict(roadDistrict);

        // Update RoadStation
        boolean updated = updateRoadStationAttributes(from, to.getRoadStation());
        to.setObsolete(to.getRoadStation().isObsolete());
        to.setObsoleteDate(to.getRoadStation().getObsoleteDate());

        return  updated ||
                hash != HashCodeBuilder.reflectionHashCode(to);
    }

    private static boolean updateRoadStationAttributes(final LamAsema from, final RoadStation to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);

        // Can insert obsolete stations
        if (POISTETUT.contains(from.getKeruunTila())) {
            to.obsolete();
        } else {
            to.setObsolete(false);
            to.setObsoleteDate(null);
        }

        to.setNaturalId(from.getVanhaId());
        to.setType(RoadStationType.LAM_STATION);
        to.setName(from.getNimi());
        to.setNameFi(from.getNimiFi());
        to.setNameSv(from.getNimiSe());
        to.setNameEn(from.getNimiEn());
        to.setDescription(from.getKuvaus());
        to.setAdditionalInformation(StringUtils.trimToNull(StringUtils.join(from.getLisatieto(), " ", from.getLisatietoja())));
        to.setLatitude(from.getLatitudi());
        to.setLongitude(from.getLongitudi());
        to.setAltitude(from.getKorkeus());
        to.setRoadNumber(from.getTieosoite().getTienumero());
        to.setRoadPart(from.getTieosoite().getTieosa());
        to.setDistanceFromRoadPartStart(from.getTieosoite().getEtaisyysTieosanAlusta());
        to.setCollectionInterval(from.getKeruuVali());
        to.setCollectionStatus(CollectionStatus.convertKeruunTila(from.getKeruunTila()));
        to.setMunicipality(from.getKunta());
        to.setMunicipalityCode(from.getKuntaKoodi());
        to.setProvince(from.getMaakunta());
        to.setProvinceCode(from.getMaakuntaKoodi());
        return hash != HashCodeBuilder.reflectionHashCode(to);
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
