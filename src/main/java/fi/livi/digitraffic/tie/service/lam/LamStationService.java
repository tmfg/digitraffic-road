package fi.livi.digitraffic.tie.service.lam;

import java.util.List;
import java.util.Map;

import fi.livi.digitraffic.tie.geojson.lamstation.LamStationFeatureCollection;
import fi.livi.digitraffic.tie.model.LamStation;

public interface LamStationService {

    /**
     * @return current non obsolete lam stations metadata as LamStationFeatureCollection
     */
    LamStationFeatureCollection findAllNonObsoleteLamStationsAsFeatureCollection();

    LamStation save(final LamStation lamStation);

    List<LamStation> findAll();

    Map<Long, LamStation> findAllLamStationsMappedByByNaturalId();
}
