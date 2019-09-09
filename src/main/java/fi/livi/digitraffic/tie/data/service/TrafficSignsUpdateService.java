package fi.livi.digitraffic.tie.data.service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.DeviceDataRepository;
import fi.livi.digitraffic.tie.data.dao.DeviceRepository;
import fi.livi.digitraffic.tie.data.model.trafficsigns.Device;
import fi.livi.digitraffic.tie.data.model.trafficsigns.DeviceData;
import fi.livi.digitraffic.tie.external.tloik.Laite;
import fi.livi.digitraffic.tie.external.tloik.LiikennemerkinTila;
import fi.livi.digitraffic.tie.external.tloik.Metatiedot;
import fi.livi.digitraffic.tie.external.tloik.Tilatiedot;

@Service
public class TrafficSignsUpdateService {
    private static final Logger log = LoggerFactory.getLogger(TrafficSignsUpdateService.class);

    private final DeviceRepository deviceRepository;
    private final DeviceDataRepository deviceDataRepository;

    public TrafficSignsUpdateService(final DeviceRepository deviceRepository, final DeviceDataRepository deviceDataRepository) {
        this.deviceRepository = deviceRepository;
        this.deviceDataRepository = deviceDataRepository;
    }

    @Transactional
    public void saveMetadata(final Metatiedot metadata) {
        final Map<String, Laite> idMap = metadata.getLaitteet().stream()
            .collect(Collectors.toMap(l -> l.getTunnus(), l -> l));

        final List<Device> devices = deviceRepository.findAllById(idMap.keySet());

        updateDevices(devices, idMap);
        // updateDevices removes updated from map
        insertDevices(idMap);

        log.debug(idMap.toString());
    }

    private void insertDevices(final Map<String, Laite> idMap) {
        final StopWatch sw = StopWatch.createStarted();

        try {
            deviceRepository.saveAll(idMap.values().stream().map(this::convertDevice).collect(Collectors.toList()));
        } finally {
            log.info("insertDeviceMetadataCount={} tookMs={}", idMap.size(), sw.getTime());
        }
    }

    private void updateDeviceInformation(final Device d, final Laite l) {
        d.setType(l.getTyyppi());
        d.setUpdatedDate(ZonedDateTime.now());
        d.setRoadAddress(l.getSijainti().getTieosoite());
        d.setEtrsTm35FinX(l.getSijainti().getE());
        d.setEtrsTm35FinY(l.getSijainti().getN());
        d.setDirection(l.getSijainti().getAjosuunta());
        d.setCarriageway(l.getSijainti().getAjorata());
    }

    private Device convertDevice(final Laite laite) {
        final Device d = new Device();
        d.setId(laite.getTunnus());

        updateDeviceInformation(d, laite);

        return d;
    }

    private void updateDevices(final List<Device> devices, final Map<String, Laite> idMap) {
        final int updateCount = devices.size();
        final StopWatch sw = StopWatch.createStarted();

        try {
            devices.forEach(d -> {
                final Laite laite = idMap.get(d.getId());

                if (laite == null) {
                    log.error("Could not find device " + d.getId());
                } else {
                    updateDeviceInformation(d, laite);

                    idMap.remove(d.getId());
                }
            });
        } finally {
            log.info("updateDeviceMetadataCount={} tookMs={}", updateCount, sw.getTime());
        }
    }

    @Transactional
    public void saveData(final Tilatiedot data) {
        final StopWatch sw = StopWatch.createStarted();

        try {
            deviceDataRepository.saveAll(data.getLiikennemerkit().stream()
                .map(this::convertData)
                .collect(Collectors.toList()));
        } finally {
            log.info("updateDeviceDataCount={} tookMs={}", data.getLiikennemerkit().size(), sw.getTime());
        }
    }

    private DeviceData convertData(final LiikennemerkinTila lt) {
        final DeviceData d = new DeviceData();

        d.setCreatedDate(ZonedDateTime.now());
        d.setAdditionalInformation(lt.getLisatieto());
        d.setCause(lt.getSyy());
        d.setDeviceId(lt.getTunnus());
        d.setDisplayValue(lt.getNayttama());
        d.setEffectDate(lt.getVoimaan());

        return d;
    }
}
