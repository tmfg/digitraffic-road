package fi.livi.digitraffic.tie.service.lam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.dao.LamStationRepository;
import fi.livi.digitraffic.tie.model.LamStationMetadata;
import fi.livi.digitraffic.tie.wsdl.lam.KeruunTILA;
import fi.livi.digitraffic.tie.wsdl.lam.LamAsema;

@Service
public class LamStationUpdater {
    private final LamStationRepository lamStationRepository;
    private final LamStationClient lamStationClient;

    private static final Logger LOG = Logger.getLogger(LamStationUpdater.class);

    @Autowired
    public LamStationUpdater(final LamStationRepository lamStationRepository, final LamStationClient lamStationClient) {
        this.lamStationRepository = lamStationRepository;
        this.lamStationClient = lamStationClient;
    }

    public void updateLamStations() {
        final List<LamAsema> stations = lamStationClient.getLamStations();
        final Map<Long, LamStationMetadata> currentStations = getCurrentStations();

        doUpdate(stations, currentStations);
    }

    private void doUpdate(final List<LamAsema> stations, final Map<Long, LamStationMetadata> currentStations) {
        final List<LamAsema> obsolete = new ArrayList<>();
        final List<LamAsema> update = new ArrayList<>();        final List<LamAsema> insert = new ArrayList<>();

        for(final LamAsema la : stations) {
            if(la.getKeruunTila() == KeruunTILA.POISTETTU_PYSYVASTI) {
                obsolete.add(la);
            } else {
                if (currentStations.containsKey(la.getId())) {
                    update.add(la);
                } else {
                    insert.add(la);
                }
            }
        }

        obsoleteLamStations(obsolete);
        updateLamStations(update);
        insertLamStations(insert);
    }

    private void insertLamStations(final List<LamAsema> insert) {
        for(final LamAsema la : insert) {
            LOG.debug("inserting new station " + la.getNimi());
        }
    }

    private void updateLamStations(final List<LamAsema> update) {
        for(final LamAsema la : update) {
            LOG.debug("updating station " + la.getNimi());
        }
    }

    private void obsoleteLamStations(final List<LamAsema> obsolete) {
        for(final LamAsema la : obsolete) {
            LOG.debug("obsolete station " + la.getNimi());
        }
    }

    private Map<Long, LamStationMetadata> getCurrentStations() {
        final List<LamStationMetadata> currentStations = lamStationRepository.findAll();
        final Map<Long, LamStationMetadata> stationMap = new HashMap<>();

        for(final LamStationMetadata lsm : currentStations) {
            stationMap.put(lsm.getLamId(), lsm);
        }

        return stationMap;
    }
}
