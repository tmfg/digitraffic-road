package fi.livi.digitraffic.tie.service;

import org.geojson.FeatureCollection;

public interface LamService {

    /**
     * @return current non obsolete lam stations metadata as FeatureCollection
     */
    FeatureCollection findAllNonObsoleteLamStationsAsFeatureCollection();
}
