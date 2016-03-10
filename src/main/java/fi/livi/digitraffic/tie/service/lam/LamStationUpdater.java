package fi.livi.digitraffic.tie.service.lam;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.model.LamStation;
import fi.livi.digitraffic.tie.model.RoadDistrict;
import fi.livi.digitraffic.tie.model.RoadStation;
import fi.livi.digitraffic.tie.service.LamStationService;
import fi.livi.digitraffic.tie.service.RoadDistrictService;
import fi.livi.digitraffic.tie.service.RoadStationService;
import fi.livi.digitraffic.tie.service.StaticDataStatusService;
import fi.livi.digitraffic.tie.wsdl.lam.KeruunTILA;
import fi.livi.digitraffic.tie.wsdl.lam.LamAsema;

@Service
public class LamStationUpdater {
    private static final Logger LOG = Logger.getLogger(LamStationUpdater.class);

    private final RoadStationService roadStationService;
    private final LamStationService lamStationService;
    private final RoadDistrictService roadDistrictService;
    private StaticDataStatusService staticDataStatusService;

    private final LamStationClient lamStationClient;

    private static final EnumSet<KeruunTILA> POISTETUT = EnumSet.of(KeruunTILA.POISTETTU_PYSYVASTI, KeruunTILA.POISTETTU_TILAPAISESTI);

    @Autowired
    public LamStationUpdater(final RoadStationService roadStationService,
                             final LamStationService lamStationService,
                             final RoadDistrictService roadDistrictService,
                             final StaticDataStatusService staticDataStatusService,
                             final LamStationClient lamStationClient) {
        this.roadStationService = roadStationService;
        this.lamStationService = lamStationService;
        this.roadDistrictService = roadDistrictService;
        this.staticDataStatusService = staticDataStatusService;
        this.lamStationClient = lamStationClient;
    }

    // 5 min
    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional
    public void updateLamStations() {
        LOG.info("updateLamStations start");
        final List<LamAsema> stations = lamStationClient.getLamStations();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Fetched LAMs:");
            for (LamAsema station : stations) {
                LOG.debug(ToStringBuilder.reflectionToString(station));
            }
        }

        final Map<Long, LamStation> currentStations = lamStationService.getAllLamStationsMappedByByNaturalId();

        final boolean updateStaticDataStatus = updateLamStations(stations, currentStations);
        updateStaticDataStatus(updateStaticDataStatus);
        LOG.info("updateLamStations done");
    }

    private void updateStaticDataStatus(final boolean updateStaticDataStatus) {
        staticDataStatusService.updateStaticDataStatus(updateStaticDataStatus);
    }

    private boolean updateLamStations(final List<LamAsema> stations, final Map<Long, LamStation> currentStations) {
        final List<LamStation> obsolete = new ArrayList<>(); // naturalIds of obsolete lam-stations
        final List<Pair<LamAsema, LamStation>> update = new ArrayList<>(); // lam-stations to update
        final List<LamAsema> insert = new ArrayList<>(); // new lam-stations

        for (final LamAsema la : stations) {
            final Long lamNaturalId = convertToLamNaturalId(la.getVanhaId());
            final LamStation currentSaved = currentStations.remove(lamNaturalId);

            if (currentSaved != null) {
                if (POISTETUT.contains(la.getKeruunTila())) {
                    obsolete.add(currentSaved);
                } else {
                    update.add(Pair.of(la, currentSaved));
                }
            } else {
                insert.add(la);
            }
        }

        // lam-stations in database, but not in server
        obsolete.addAll(currentStations.values());

        obsoleteLamStations(obsolete);
        updateLamStations(update);
        final boolean inserted = insertLamStations(insert);

        return !obsolete.isEmpty() || inserted;
    }

    /**
     * @param roadStationVanhaId LamAsema.vanhaId
     * @return
     */
    private static Long convertToLamNaturalId(final Integer roadStationVanhaId) {
        return roadStationVanhaId == null ? null : roadStationVanhaId - 23000L;
    }

    private boolean insertLamStations(final List<LamAsema> insert) {
        boolean inserted = false;

        for (final LamAsema la : insert) {
            LOG.debug("inserting new station " + la.getNimi());

            if (validate(la)) {
                insertLamStation(la);
                inserted = true;
            } else {
                LOG.debug("validate failed for " + la.getNimi());
            }
        }

        return inserted;
    }

    private void insertLamStation(final LamAsema la) {
        Integer roadNaturalId = la.getTieosoite().getTienumero();
        Integer roadSectionNaturalId = la.getTieosoite().getTieosa();
        RoadDistrict roadDistrict = roadDistrictService.findByRoadSectionAndRoadNaturalId(roadSectionNaturalId, roadNaturalId);

        if (roadDistrict != null) {
            final RoadStation newRoadStation = roadStationService.createRoadStation(la);
            final LamStation newLamStation = createLamStation(la, newRoadStation, roadDistrict);
            roadStationService.save(newRoadStation);
            lamStationService.save(newLamStation);
        } else {
            LOG.error("LamStation insert failed: Could not find RoadDistrict for LAM station (" + la.getVanhaId() + ") " + la.getNimi() + " with roadSectionNaturalId " + roadSectionNaturalId + ", roadNaturalId: " + roadNaturalId + ", ");
        }
    }

    private static boolean validate(final LamAsema la) {
        return la.getVanhaId() != null && isNotEmpty(la.getNimi()) && isNotEmpty(la.getNimiEn()) && isNotEmpty(la.getNimiFi()) &&
                isNotEmpty(la.getNimiSe()) && la.getTieosoite() != null && la.getTieosoite().getTienumero() != null &&
                la.getTieosoite().getTieosa() != null;
    }

    private static LamStation createLamStation(final LamAsema la, final RoadStation newRoadStation, final RoadDistrict roadDistrict) {
        final LamStation ls = new LamStation();

        ls.setNaturalId(convertToLamNaturalId(la.getVanhaId()));
        ls.setName(la.getNimiFi());
        ls.setObsolete(false);
        ls.setObsoleteDate(null);
        ls.setSummerFreeFlowSpeed1(0);
        ls.setSummerFreeFlowSpeed2(0);
        ls.setWinterFreeFlowSpeed1(0);
        ls.setWinterFreeFlowSpeed2(0);
        ls.setRoadDistrict(roadDistrict);
        ls.setRoadStation(newRoadStation);

        return ls;
    }

    private void updateLamStations(final List<Pair<LamAsema, LamStation>> update) {
        for (final Pair<LamAsema, LamStation> pair : update) {

            final LamAsema la = pair.getLeft();
            final LamStation ls = pair.getRight();

            LOG.debug("updating station " + la.getNimi());

            ls.setObsolete(false);
            ls.setObsoleteDate(null);
            ls.setName(la.getNimi());
            ls.setDirection1Municipality(la.getSuunta1Kunta());
            ls.setDirection1MunicipalityCode(la.getSuunta1KuntaKoodi());
            ls.setDirection2Municipality(la.getSuunta2Kunta());
            ls.setDirection2MunicipalityCode(la.getSuunta2KuntaKoodi());

            Integer roadNaturalId = la.getTieosoite().getTienumero();
            Integer roadSectionNaturalId = la.getTieosoite().getTieosa();
            RoadDistrict rd = roadDistrictService.findByRoadSectionAndRoadNaturalId(roadSectionNaturalId, roadNaturalId);
            if (rd == null) {
                LOG.warn("LamStation update: Could not find RoadDistrict for LAM station (" + convertToLamNaturalId(la.getVanhaId()) + ") " + la.getNimi() + " current Road District: " + ls.getRoadDistrict().getNaturalId() + " with roadSectionNaturalId " + roadSectionNaturalId + " vs old: " + ls.getRoadStation().getRoadPart() + ", roadNaturalId: " + roadNaturalId + " vs old: " + ls.getRoadStation().getRoadNumber());
            } else {
                if (ls.getRoadDistrict().getNaturalId() != rd.getNaturalId()) {
                    LOG.info("Update LAM station (" + convertToLamNaturalId(la.getVanhaId()) + ") " + la.getNimi() + " road district " + ls.getRoadDistrict().getNaturalId() + " -> " + rd.getNaturalId());
                    ls.setRoadDistrict(rd);
                }
            }

            updateRoadStationAttributes(la, ls.getRoadStation());
        }
    }

    private static void updateRoadStationAttributes(final LamAsema la, final RoadStation rs) {
        rs.setObsolete(false);
        rs.setObsoleteDate(null);
        rs.setName(la.getNimi());
        rs.setNameFi(la.getNimiFi());
        rs.setNameSe(la.getNimiSe());
        rs.setNameEn(la.getNimiEn());
        rs.setLatitude(la.getLatitudi());
        rs.setLongitude(la.getLongitudi());
        rs.setAltitude(la.getKorkeus());
        rs.setRoadNumber(la.getTieosoite().getTienumero());
        rs.setRoadPart(la.getTieosoite().getTieosa());
        rs.setDistance(la.getTieosoite().getEtaisyysTieosanAlusta());
    }

    private static void obsoleteLamStations(final List<LamStation> obsolete) {
        for (final LamStation station : obsolete) {
            LOG.debug("Obsolete station " + station.getName());
            station.obsolete();
        }
    }
}
