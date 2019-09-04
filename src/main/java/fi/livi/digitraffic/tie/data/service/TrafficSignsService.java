package fi.livi.digitraffic.tie.data.service;

import java.math.BigDecimal;
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
import fi.livi.digitraffic.tie.data.dto.trafficsigns.*;
import fi.livi.digitraffic.tie.data.dto.trafficsigns.DeviceMetadataSchema;
import fi.livi.digitraffic.tie.data.model.trafficsigns.Device;
import fi.livi.digitraffic.tie.data.model.trafficsigns.DeviceData;

@Service
public class TrafficSignsService {
    private static final Logger log = LoggerFactory.getLogger(TrafficSignsService.class);

    private final DeviceRepository deviceRepository;
    private final DeviceDataRepository deviceDataRepository;

    public TrafficSignsService(final DeviceRepository deviceRepository, final DeviceDataRepository deviceDataRepository) {
        this.deviceRepository = deviceRepository;
        this.deviceDataRepository = deviceDataRepository;
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
            deviceRepository.saveAll(idMap.values().stream().map(this::convertDevice).collect(Collectors.toList()));
        } finally {
            log.info("insertDeviceMetadataCount={} tookMs={}", idMap.size(), sw.getTime());
        }
    }

    private Device convertDevice(final DeviceMetadataSchema md) {
        final Device d = new Device();

        d.setId(md.tunnus);
        d.setType(md.tyyppi);
        d.setUpdatedDate(ZonedDateTime.now());
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
        final StopWatch sw = StopWatch.createStarted();

        try {
            deviceDataRepository.saveAll(data.liikennemerkit.stream().map(this::convertData).collect(Collectors.toList()));
        } finally {
            log.info("updateDeviceDataCount={} tookMs={}", data.liikennemerkit.size(), sw.getTime());
        }
    }

    private DeviceData convertData(final DeviceDataSchema ds) {
        final DeviceData d = new DeviceData();

        d.setCreatedDate(ZonedDateTime.now());
        d.setAdditionalInformation(ds.lisatieto);
        d.setCause(ds.syy);
        d.setDeviceId(ds.tunnus);
        d.setDisplayValue(ds.nayttama);
        d.setEffectDate(ds.voimaan);

        return d;
    }
}
