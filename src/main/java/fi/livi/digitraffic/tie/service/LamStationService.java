package fi.livi.digitraffic.tie.service;

import java.util.List;
import java.util.Map;

import fi.livi.digitraffic.tie.model.LamStation;
import org.geojson.FeatureCollection;

public interface LamStationService {

    /**
     * @return current non obsolete lam stations metadata as FeatureCollection
     */
    FeatureCollection findAllNonObsoleteLamStationsAsFeatureCollection();

    LamStation save(LamStation lamStation);

    List<LamStation> findAll();

    Map<Long, LamStation> getAllLamStationsMappedByByNaturalId();
}
