package fi.livi.digitraffic.tie.service.lam;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.LamStationRepository;
import fi.livi.digitraffic.tie.dao.RoadDistrictRepository;
import fi.livi.digitraffic.tie.dao.RoadStationRepository;
import fi.livi.digitraffic.tie.model.LamStation;
import fi.livi.digitraffic.tie.model.RoadDistrict;
import fi.livi.digitraffic.tie.model.RoadStation;
import fi.livi.digitraffic.tie.wsdl.lam.KeruunTILA;
import fi.livi.digitraffic.tie.wsdl.lam.LamAsema;

@Service
public class LamStationUpdater {
    private final LamStationRepository lamStationRepository;
    private final RoadStationRepository roadStationRepository;
    private final RoadDistrictRepository roadDistrictRepository;

    private final LamStationClient lamStationClient;

    private static final Logger LOG = Logger.getLogger(LamStationUpdater.class);

    @Autowired
    public LamStationUpdater(final LamStationRepository lamStationRepository1, final RoadStationRepository roadStationRepository, final RoadDistrictRepository roadDistrictRepository, final LamStationClient lamStationClient) {
        this.lamStationRepository = lamStationRepository1;
        this.roadStationRepository = roadStationRepository;
        this.roadDistrictRepository = roadDistrictRepository;
        this.lamStationClient = lamStationClient;
    }

    @Transactional
    public void updateLamStations() {
        final List<LamAsema> stations = lamStationClient.getLamStations();
        final Map<Long, LamStation> currentStations = getCurrentStations();

        doUpdate(stations, currentStations);
    }

    private void doUpdate(final List<LamAsema> stations, final Map<Long, LamStation> currentStations) {
        final List<LamStation> obsolete = new ArrayList<>(); // naturalIds of obsolete lam-stations
        final List<Pair<LamAsema, LamStation>> update = new ArrayList<>(); // lam-stations to update
        final List<LamAsema> insert = new ArrayList<>(); // new lam-stations

        for(final LamAsema la : stations) {
            final Long lamNaturalId = convertLamNaturalId(la.getVanhaId());
            final LamStation saved = currentStations.remove(lamNaturalId);

            if(saved != null) {
                if(la.getKeruunTila() == KeruunTILA.POISTETTU_PYSYVASTI) {
                    obsolete.add(saved);
                } else {
                    update.add(Pair.of(la, saved));
                }
            } else {
                insert.add(la);
            }
        }

        // lam-stations in database, but not in server
        obsolete.addAll(currentStations.values());

        obsoleteLamStations(obsolete);
        updateLamStations(update);
        insertLamStations(insert);
    }

    private static Long convertLamNaturalId(final Integer roadStationId) {
        return roadStationId == null ? null : roadStationId - 23000L;
    }

    private void insertLamStations(final List<LamAsema> insert) {
        for(final LamAsema la : insert) {
            LOG.debug("inserting new station " + la.getNimi());

            if(validate(la)) {
                insertLamStation(la);
            } else {
                LOG.debug("validate failed for " + la.getNimi());
            }
        }
    }

    private void insertLamStation(final LamAsema la) {
        final int roadDistrictNumber = la.getTieosoite().getTienumero();
        final RoadDistrict roadDistrict = roadDistrictRepository.findByNaturalId(roadDistrictNumber);

        if(roadDistrict != null) {
            final RoadStation newRoadStation = createRoadStation(la);
            final LamStation newLamStation = createLamStation(la, newRoadStation, roadDistrict);

            roadStationRepository.save(newRoadStation);
            lamStationRepository.save(newLamStation);
        } else {
            LOG.debug("could not find roaddistrict " + roadDistrictNumber);
        }
    }

    private boolean validate(final LamAsema la) {
        return la.getVanhaId() != null && isNotEmpty(la.getNimi()) && isNotEmpty(la.getNimiEn()) && isNotEmpty(la.getNimiFi()) &&
                isNotEmpty(la.getNimiSe()) && la.getTieosoite() != null && la.getTieosoite().getTienumero() != null;
    }

    private static RoadStation createRoadStation(final LamAsema la) {
        final RoadStation rs = new RoadStation();

        rs.setNaturalId(la.getVanhaId());
        rs.setType(1); // LAM-STATION
        rs.setObsolete(false);
        rs.setObsoleteDate(null);
        rs.setName(la.getNimi());
        rs.setNameFi(la.getNimiFi());
        rs.setNameSe(la.getNimiSe());
        rs.setNameEn(la.getNimiEn());
        rs.setLatitude(la.getLatitudi());
        rs.setLongitude(la.getLongitudi());
        rs.setElevation(la.getKorkeus());

        return rs;
    }

    private static LamStation createLamStation(final LamAsema la, final RoadStation newRoadStation, final RoadDistrict roadDistrict) {
        final LamStation ls = new LamStation();

        ls.setNaturalId(convertLamNaturalId(la.getVanhaId()));
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
        for(final Pair<LamAsema, LamStation> pair : update) {
            final LamAsema la = pair.getLeft();
            final LamStation ls = pair.getRight();

            LOG.debug("updating station " + la.getNimi());

            ls.setName(la.getNimiFi());
            updateAttributes(la, ls.getRoadStation());
        }
    }

    private static void updateAttributes(final LamAsema la, final RoadStation rs) {
        rs.setName(la.getNimi());
        rs.setNameFi(la.getNimiFi());
        rs.setNameSe(la.getNimiSe());
        rs.setNameEn(la.getNimiEn());
        rs.setLatitude(la.getLatitudi());
        rs.setLongitude(la.getLongitudi());
        rs.setElevation(la.getKorkeus());
    }

    private void obsoleteLamStations(final List<LamStation> obsolete) {
        for(final LamStation station : obsolete) {
            LOG.debug("obsolete station " + station.getName());

            station.obsolete();
        }
    }

    private Map<Long, LamStation> getCurrentStations() {
        final List<LamStation> allStations = lamStationRepository.findAll();
        final Map<Long, LamStation> stationMap = new HashMap<>();

        for(final LamStation lam : allStations) {
            stationMap.put(lam.getNaturalId(), lam);
        }

        return stationMap;
    }
}
