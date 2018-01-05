package fi.livi.digitraffic.tie.metadata.service.roadstation;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuCameraStationMetadataService;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuTmsStationMetadataService;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuWeatherStationMetadataService;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.KameraVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.LamAsemaVO;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.TiesaaAsemaVO;

@Service
public class RoadStationStatusUpdater {
    private static final Logger log = LoggerFactory.getLogger(RoadStationStatusUpdater.class);

    private final LotjuCameraStationMetadataService lotjuCameraStationMetadataService;
    private final LotjuTmsStationMetadataService lotjuTmsStationMetadataService;
    private final LotjuWeatherStationMetadataService lotjuWeatherStationMetadataService;
    private RoadStationService roadStationService;

    public RoadStationStatusUpdater(final LotjuCameraStationMetadataService lotjuCameraStationMetadataService,
                                    final LotjuTmsStationMetadataService lotjuTmsStationMetadataService,
                                    final LotjuWeatherStationMetadataService lotjuWeatherStationMetadataService,
                                    final RoadStationService roadStationService) {
        this.lotjuCameraStationMetadataService = lotjuCameraStationMetadataService;
        this.lotjuTmsStationMetadataService = lotjuTmsStationMetadataService;
        this.lotjuWeatherStationMetadataService = lotjuWeatherStationMetadataService;
        this.roadStationService = roadStationService;
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    public int updateTmsStationsStatuses() {
        log.info("Update TMS stations statuses");
        final List<LamAsemaVO> allLams = lotjuTmsStationMetadataService.getLamAsemas();

        int updated = 0;
        for(LamAsemaVO from : allLams) {
            try {
                if (roadStationService.updateRoadStation(from)) {
                    updated++;
                }
            } catch (Exception e) {
                    log.error("method=updateTmsStationsStatuses : Updating roadstation nimiFi={} lotjuId={} naturalId={} failed", from.getNimiFi(), from.getId(), from.getVanhaId());
                throw e;
            }
        }
        return updated;
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    public int updateWeatherStationsStatuses() {
        log.info("Update weather stations statuses");
        final List<TiesaaAsemaVO> allTiesaaAsemas = lotjuWeatherStationMetadataService.getTiesaaAsemmas();

        int updated = 0;
        for (TiesaaAsemaVO from : allTiesaaAsemas) {
            try {
            if (roadStationService.updateRoadStation(from)) {
                updated++;
            }
            } catch (Exception e) {
                log.error("method=updateWeatherStationsStatuses : Updating roadstation nimiFi={} lotjuId={} naturalId={} failed", from.getNimiFi(), from.getId(), from.getVanhaId());
                throw e;
            }
        }
        return updated;
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    public int updateCameraStationsStatuses() {
        log.info("Update camera stations statuses");
        final List<KameraVO> allKameras = lotjuCameraStationMetadataService.getKameras();

        int updated = 0;
        for (KameraVO from : allKameras) {
            try {
                if (roadStationService.updateRoadStation(from)) {
                    updated++;
                }
            } catch (Exception e) {
                log.error("method=updateCameraStationsStatuses : Updating roadstation nimiFi={} lotjuId={} naturalId={} failed", from.getNimiFi(), from.getId(), from.getVanhaId());
                throw e;
            }
        };
        return updated;
    }
}
