package fi.livi.digitraffic.tie.metadata.service.lam;

import java.util.Map;

import fi.livi.digitraffic.tie.metadata.geojson.lamstation.LamStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.LamStation;

public interface LamStationService {

    /**
     * @return current non obsolete lam stations metadata as LamStationFeatureCollection
     */
    LamStationFeatureCollection findAllNonObsoletePublicLamStationsAsFeatureCollection(boolean onlyUpdateInfo);

    LamStation save(final LamStation lamStation);

    Map<Long, LamStation> findAllLamStationsMappedByByNaturalId();

    LamStation findByLotjuId(long lamStationLotjuId);
}
