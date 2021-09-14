package fi.livi.digitraffic.tie.service.v1;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fi.livi.digitraffic.tie.external.tloik.variablesigns.Rivi;
import fi.livi.digitraffic.tie.model.v2.trafficsigns.DeviceDataRow;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.v2.V2DeviceDataRepository;
import fi.livi.digitraffic.tie.dao.v2.V2DeviceRepository;
import fi.livi.digitraffic.tie.external.tloik.variablesigns.Laite;
import fi.livi.digitraffic.tie.external.tloik.variablesigns.LiikennemerkinTila;
import fi.livi.digitraffic.tie.external.tloik.variablesigns.Metatiedot;
import fi.livi.digitraffic.tie.external.tloik.variablesigns.Tilatiedot;
import fi.livi.digitraffic.tie.model.v2.trafficsigns.Device;
import fi.livi.digitraffic.tie.model.v2.trafficsigns.DeviceData;

@Service
public class VariableSignUpdateService {
    private static final Logger log = LoggerFactory.getLogger(VariableSignUpdateService.class);

    private final V2DeviceRepository v2DeviceRepository;
    private final V2DeviceDataRepository v2DeviceDataRepository;

    public VariableSignUpdateService(final V2DeviceRepository v2DeviceRepository, final V2DeviceDataRepository v2DeviceDataRepository) {
        this.v2DeviceRepository = v2DeviceRepository;
        this.v2DeviceDataRepository = v2DeviceDataRepository;
    }

    @Transactional
    public void saveMetadata(final Metatiedot metadata) {
        final Map<String, Laite> idMap = metadata.getLaitteet().stream()
            .collect(Collectors.toMap(l -> l.getTunnus(), l -> l));

        final List<Device> devices = v2DeviceRepository.findAllById(idMap.keySet());

        updateDevices(devices, idMap);
        // updateDevices removes updated from map
        insertDevices(idMap);

        log.debug(idMap.toString());
    }

    private void insertDevices(final Map<String, Laite> idMap) {
        final StopWatch sw = StopWatch.createStarted();

        try {
            v2DeviceRepository.saveAll(idMap.values().stream().map(this::convertDevice).collect(Collectors.toList()));
        } finally {
            log.info("insertDeviceMetadataCount={} tookMs={}", idMap.size(), sw.getTime());
        }
    }

    private void updateDeviceInformation(final Device d, final Laite l) {
        d.setType(l.getTyyppi());
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
            final List<DeviceData> newData = data.getLiikennemerkit().stream()
                .map(this::convertData)
                .collect(Collectors.toList());

//            findDuplicates(newData);

            v2DeviceDataRepository.saveAll(newData);
        } finally {
            log.info("updateDeviceDataCount={} tookMs={}", data.getLiikennemerkit().size(), sw.getTime());
        }
    }

    private void findDuplicates(final List<DeviceData> dataList) {
        final List<List<DeviceData>> duplicates = dataList.stream()
            .collect(Collectors.groupingBy(DeviceData::getDeviceId))
            .entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        duplicates.forEach(duplicateList -> {
            final String list = duplicateList.stream()
                .map(dd -> String.format("%s %s %s %s", dd.getDeviceId(), dd.getDisplayValue(), dd.getEffectDate(), dd.getCreatedDate()))
                .collect(Collectors.joining("\n"));
            log.info("method=findDuplicates duplicates " + list);
        });
    }

    private DeviceData convertData(final LiikennemerkinTila lt) {
        final DeviceData d = new DeviceData();

        d.setCreatedDate(ZonedDateTime.now());
        d.setAdditionalInformation(lt.getLisatieto());
        d.setCause(lt.getSyy());
        d.setDeviceId(lt.getTunnus());
        d.setDisplayValue(lt.getNayttama());
        d.setEffectDate(lt.getVoimaan());
        d.setReliability(lt.getLuotettavuus());
        d.setRows(convertRows(lt.getRivit()));

        return d;
    }

    private List<DeviceDataRow> convertRows(final List<Rivi> rivit) {
        return rivit.stream().map(r -> {
            final DeviceDataRow row = new DeviceDataRow();

            row.setScreen(r.getNaytto());
            row.setRowNumber(r.getRivi());
            row.setText(r.getTeksti());

            return row;
        }).collect(Collectors.toList());
    }
}
