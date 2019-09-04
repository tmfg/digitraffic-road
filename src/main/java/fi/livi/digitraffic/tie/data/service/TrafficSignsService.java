package fi.livi.digitraffic.tie.data.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.DeviceRepository;
import fi.livi.digitraffic.tie.data.dto.trafficsigns.DataSchema;
import fi.livi.digitraffic.tie.data.dto.trafficsigns.DeviceMetadataSchema;
import fi.livi.digitraffic.tie.data.dto.trafficsigns.MetadataSchema;
import fi.livi.digitraffic.tie.data.model.trafficsigns.Device;

@Service
public class TrafficSignsService {
    private static final Logger log = LoggerFactory.getLogger(TrafficSignsService.class);

    private final DeviceRepository deviceRepository;

    public TrafficSignsService(final DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Transactional
    public void saveMetadata(final MetadataSchema metadata) {
        final Map<String, DeviceMetadataSchema> idMap = metadata.laitteet.stream().collect(Collectors.toMap(l -> l.tunnus, l -> l));

        final List<Device> devices = deviceRepository.findAllById(idMap.keySet());

        updateDevices(devices, idMap);
        // updateDevices removes updated from map
        insertDevices(idMap);

        log.debug(idMap.toString());
    }

    private void insertDevices(final Map<String, DeviceMetadataSchema> idMap) {
        final StopWatch sw = StopWatch.createStarted();

        try {
            deviceRepository.saveAll(idMap.values().stream().map(this::convert).collect(Collectors.toList()));
        } finally {
            log.info("insertDeviceMetadataCount={} tookMs={}", idMap.size(), sw.getTime());
        }
    }

    private Device convert(final DeviceMetadataSchema md) {
        final Device d = new Device();

        d.setId(md.tunnus);
        d.setType(md.tyyppi);
        d.setRoadAddress(md.tieosoite);
        d.setEtrsTm35FinX(doubleToBD(md.etrsTm35FinX));
        d.setEtrsTm35FinY(doubleToBD(md.etrsTm35FinY));

        return d;
    }

    private BigDecimal doubleToBD(final Double d) {
        return d == null ? null : BigDecimal.valueOf(d);
    }

    private void updateDevices(final List<Device> devices, final Map<String, DeviceMetadataSchema> idMap) {
        final int updateCount = devices.size();
        final StopWatch sw = StopWatch.createStarted();

        try {
            devices.forEach(d -> {
                final DeviceMetadataSchema ms = idMap.get(d.getId());

                if (ms == null) {
                    log.error("Could not find device " + d.getId());
                } else {
                    d.setType(ms.tyyppi);
                    d.setRoadAddress(ms.tieosoite);
                    d.setEtrsTm35FinX(doubleToBD(ms.etrsTm35FinX));
                    d.setEtrsTm35FinY(doubleToBD(ms.etrsTm35FinY));

                    idMap.remove(d.getId());
                }
            });
        } finally {
            log.info("updateDeviceMetadataCount={} tookMs={}", updateCount, sw.getTime());
        }
    }

    public void saveData(final DataSchema data) {
        // TODO
    }
}
