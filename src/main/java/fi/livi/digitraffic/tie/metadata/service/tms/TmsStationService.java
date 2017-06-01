package fi.livi.digitraffic.tie.metadata.service.tms;

import static fi.livi.digitraffic.tie.metadata.model.CollectionStatus.isPermanentlyDeletedKeruunTila;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.controller.TmsState;
import fi.livi.digitraffic.tie.metadata.converter.NonPublicRoadStationException;
import fi.livi.digitraffic.tie.metadata.converter.TmsStationMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.metadata.dao.RoadAddressRepository;
import fi.livi.digitraffic.tie.metadata.dao.tms.TmsStationRepository;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.CalculatorDeviceType;
import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.TmsStation;
import fi.livi.digitraffic.tie.metadata.model.TmsStationType;
import fi.livi.digitraffic.tie.metadata.service.RoadDistrictService;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;
import fi.livi.digitraffic.tie.metadata.service.UpdateStatus;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.LamAsemaVO;

@Service
public class TmsStationService extends AbstractTmsStationAttributeUpdater {
    private static final Logger log = LoggerFactory.getLogger(TmsStationService.class);

    private final TmsStationRepository tmsStationRepository;
    private final StaticDataStatusService staticDataStatusService;
    private final RoadStationService roadStationService;
    private final RoadDistrictService roadDistrictService;
    private final TmsStationMetadata2FeatureConverter tmsStationMetadata2FeatureConverter;
    private final RoadAddressRepository roadAddressRepository;

    @Autowired
    public TmsStationService(final TmsStationRepository tmsStationRepository,
                             final StaticDataStatusService staticDataStatusService,
                             final RoadStationService roadStationService,
                             final RoadDistrictService roadDistrictService,
                             final TmsStationMetadata2FeatureConverter tmsStationMetadata2FeatureConverter,
                             final RoadAddressRepository roadAddressRepository) {
        super(log);
        this.tmsStationRepository = tmsStationRepository;
        this.staticDataStatusService = staticDataStatusService;
        this.roadStationService = roadStationService;
        this.roadDistrictService = roadDistrictService;
        this.tmsStationMetadata2FeatureConverter = tmsStationMetadata2FeatureConverter;
        this.roadAddressRepository = roadAddressRepository;
    }

    @Transactional(readOnly = true)
    public TmsStationFeatureCollection findAllPublishableTmsStationsAsFeatureCollection(final boolean onlyUpdateInfo,
        final TmsState tmsState) {
        final MetadataUpdated updated = staticDataStatusService.findMetadataUpdatedByMetadataType(MetadataType.LAM_STATION);
        final List<TmsStation> stations = findStations(onlyUpdateInfo, tmsState);

        return tmsStationMetadata2FeatureConverter.convert(
                stations,
                updated != null ? updated.getUpdatedTime() : null);
    }

    @Transactional(readOnly = true)
    public TmsStationFeatureCollection listTmsStationsByRoadNumber(final Integer roadNumber, final TmsState tmsState) {
        final MetadataUpdated updated = staticDataStatusService.findMetadataUpdatedByMetadataType(MetadataType.LAM_STATION);
        final List<TmsStation> stations = findStations(roadNumber, tmsState);

        return tmsStationMetadata2FeatureConverter.convert(
            stations,
            updated != null ? updated.getUpdatedTime() : null);
    }

    @Transactional(readOnly = true)
    public TmsStationFeature getTmsStationByRoadStationId(final Long roadStationId) throws NonPublicRoadStationException {
        final TmsStation station = tmsStationRepository.findByRoadStationIsPublicIsTrueAndRoadStation_NaturalId(roadStationId);

        return convert(roadStationId, station);
    }

    @Transactional(readOnly = true)
    public TmsStationFeature getTmsStationByLamId(final Long lamId) throws NonPublicRoadStationException {
        return convert(lamId, tmsStationRepository.findByRoadStationIsPublicIsTrueAndNaturalId(lamId));
    }


    @Transactional
    public List<TmsStation> findAllPublishableTmsStations() {
        return tmsStationRepository.findByRoadStationPublishableIsTrueOrderByRoadStation_NaturalId();
    }

    @Transactional(readOnly = true)
    public Map<Long, TmsStation> findAllTmsStationsMappedByByTmsNaturalId() {
        final List<TmsStation> allStations = tmsStationRepository.findAll();

        return allStations.stream().collect(Collectors.toMap(TmsStation::getNaturalId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public Map<Long, TmsStation> findAllTmsStationsMappedByByLotjuId() {
        final List<TmsStation> allStations = tmsStationRepository.findAll();

        return allStations.stream().filter(tms -> tms.getLotjuId() != null).collect(Collectors.toMap(TmsStation::getLotjuId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public Map<Long, TmsStation> findAllTmsStationsByMappedByLotjuId() {
        final Map<Long, TmsStation> map = new HashMap<>();
        final List<TmsStation> all = tmsStationRepository.findAll();
        for (final TmsStation tmsStation : all) {
            if (tmsStation.getLotjuId() != null) {
                map.put(tmsStation.getLotjuId(), tmsStation);
            } else {
                log.warn("Null lotjuId: " + tmsStation);
            }
        }
        return map;
    }

    @Transactional(readOnly = true)
    public Map<Long, TmsStation> findAllPublishableTmsStationsMappedByLotjuId() {
        final List<TmsStation> all = findAllPublishableTmsStations();

        return all.stream().collect(Collectors.toMap(TmsStation::getLotjuId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public TmsStation findPublishableTmsStationByRoadStationNaturalId(long roadStationNaturalId) {
        final TmsStation entity = tmsStationRepository.findByRoadStation_NaturalIdAndRoadStationPublishableIsTrue(roadStationNaturalId);

        if (entity == null) {
            throw new ObjectNotFoundException(TmsStation.class, roadStationNaturalId);
        }

        return entity;
    }

    @Transactional(readOnly = true)
    public boolean tmsStationExistsWithRoadStationNaturalId(long roadStationNaturalId) {
        return tmsStationRepository.tmsExistsWithRoadStationNaturalId(roadStationNaturalId);
    }

    @Transactional(readOnly = true)
    public Map<Long, TmsStation> findAllTmsStationsWithoutLotjuIdMappedByTmsNaturalId() {
        final List<TmsStation> all = tmsStationRepository.findByLotjuIdIsNull();

        return all.stream().collect(Collectors.toMap(TmsStation::getNaturalId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public List<TmsStation> findTmsStationsWithoutRoadStation() {
        return tmsStationRepository.findByRoadStationIsNull();
    }

    @Transactional
    public int fixNullLotjuIds(List<LamAsemaVO> lamAsemas) {
        Map<Long, TmsStation> naturalIdToWeatherStationMap =
            findAllTmsStationsWithoutLotjuIdMappedByTmsNaturalId();
        int updated = 0;
        for (LamAsemaVO lamAsema : lamAsemas) {
            TmsStation ws = lamAsema.getVanhaId() != null ?
                                naturalIdToWeatherStationMap.get(lamAsema.getVanhaId().longValue()) : null;
            if (ws != null) {
                ws.setLotjuId(lamAsema.getId());
                ws.getRoadStation().setLotjuId(lamAsema.getId());
                updated++;
            }
        }
        if (updated > 0) {
            log.info("Fixed null lotjuIds for {} tms stations", updated);
        }
        return updated;
    }

    @Transactional
    public UpdateStatus updateOrInsertTmsStation(LamAsemaVO lam) {
        TmsStation existingTms = findTmsStationByLotjuId(lam.getId());

        if (existingTms != null) {
            final int hash = HashCodeBuilder.reflectionHashCode(existingTms);
            final String before = ReflectionToStringBuilder.toString(existingTms);

            RoadStation rs = existingTms.getRoadStation();
            if (rs == null) {
                rs = roadStationService.findByTypeAndNaturalId(RoadStationType.TMS_STATION, lam.getVanhaId().longValue());
                existingTms.setRoadStation(rs);
            }
            if (rs == null) {
                rs = new RoadStation(RoadStationType.WEATHER_STATION);
                existingTms.setRoadStation(rs);
                roadStationService.save(rs);
            }
            if (setRoadAddressIfNotSet(rs)) {
                roadAddressRepository.save(rs.getRoadAddress());
            }

            if ( updateTmsStationAttributes(lam, existingTms) ||
                hash != HashCodeBuilder.reflectionHashCode(existingTms) ) {
                log.info("Updated:\n{} ->\n{}", before, ReflectionToStringBuilder.toString(existingTms));
                return UpdateStatus.UPDATED;
            }
            return UpdateStatus.NOT_UPDATED;
        } else {
            final TmsStation newTms = new TmsStation();
            newTms.setRoadStation(new RoadStation(RoadStationType.TMS_STATION));
            setRoadAddressIfNotSet(newTms.getRoadStation());
            updateTmsStationAttributes(lam, newTms);
            tmsStationRepository.save(newTms);

            log.info("Created new {}", newTms);
            return UpdateStatus.INSERTED;
        }
    }

    private List<TmsStation> findStations(final Integer roadNumber, final TmsState tmsState) {
        switch(tmsState) {
        case ACTIVE:
            return tmsStationRepository
                .findByRoadStationPublishableIsTrueAndRoadStationRoadAddressRoadNumberIsOrderByRoadStation_NaturalId(roadNumber);
        case REMOVED:
            return tmsStationRepository
                .findByRoadStationIsPublicIsTrueAndRoadStationCollectionStatusIsAndRoadStationRoadAddressRoadNumberIsOrderByRoadStation_NaturalId
                    (CollectionStatus.REMOVED_PERMANENTLY, roadNumber);
        case ALL:
            return tmsStationRepository
                .findByRoadStationIsPublicIsTrueAndRoadStationRoadAddressRoadNumberIsOrderByRoadStation_NaturalId(roadNumber);
        default:
            throw new IllegalArgumentException();
        }
    }

    private List<TmsStation> findStations(final boolean onlyUpdateInfo, final TmsState tmsState) {
        if(onlyUpdateInfo) {
            return Collections.emptyList();
        }

        switch(tmsState) {
        case ACTIVE:
            return tmsStationRepository.findByRoadStationPublishableIsTrueOrderByRoadStation_NaturalId();
        case REMOVED:
            return tmsStationRepository.findByRoadStationIsPublicIsTrueAndRoadStationCollectionStatusIsOrderByRoadStation_NaturalId
                (CollectionStatus.REMOVED_PERMANENTLY);
        case ALL:
            return tmsStationRepository.findByRoadStationIsPublicIsTrueOrderByRoadStation_NaturalId();
        default:
            throw new IllegalArgumentException();
        }
    }

    private boolean updateTmsStationAttributes(final LamAsemaVO from, final TmsStation to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);
        to.setNaturalId(convertToTmsNaturalId(from.getVanhaId()));
        to.setLotjuId(from.getId());

        to.setName(from.getNimi());
        to.setDirection1Municipality(from.getSuunta1Kunta());
        to.setDirection1MunicipalityCode(from.getSuunta1KuntaKoodi());
        to.setDirection2Municipality(from.getSuunta2Kunta());
        to.setDirection2MunicipalityCode(from.getSuunta2KuntaKoodi());
        to.setTmsStationType(TmsStationType.convertFromLamasemaTyyppi(from.getTyyppi()));
        to.setCalculatorDeviceType(CalculatorDeviceType.convertFromLaiteTyyppi(from.getLaskinlaite()));

        final Integer roadNaturalId = from.getTieosoite() != null ? from.getTieosoite().getTienumero() : null;
        final Integer roadSectionNaturalId = from.getTieosoite() != null ? from.getTieosoite().getTieosa() : null;

        if (roadNaturalId != null && roadSectionNaturalId != null) {
            to.setRoadDistrict(roadDistrictService.findByRoadSectionAndRoadNaturalId(roadSectionNaturalId, roadNaturalId));
            if (to.getRoadDistrict() == null && !isPermanentlyDeletedKeruunTila(from.getKeruunTila())) {
                log.warn("Could not find RoadDistrict with roadSectionNaturalId: {}, roadNaturalId: {} for {}",
                         roadSectionNaturalId, roadNaturalId, ToStringHelper.toString(from));
            }
        } else {
            to.setRoadDistrict(null);
        }

        // Update RoadStation
        final boolean updated = updateRoadStationAttributes(from, to.getRoadStation());
        to.setObsolete(to.getRoadStation().isObsolete());
        to.setObsoleteDate(to.getRoadStation().getObsoleteDate());

        return updated ||
            hash != HashCodeBuilder.reflectionHashCode(to);
    }

    private TmsStation findTmsStationByLotjuId(final Long lotjuId) {
        return tmsStationRepository.findByLotjuId(lotjuId);
    }

    private static Long convertToTmsNaturalId(final Integer roadStationVanhaId) {
        return roadStationVanhaId == null ? null : roadStationVanhaId - 23000L;
    }

    private TmsStationFeature convert(final Long id, final TmsStation station) throws NonPublicRoadStationException {
        if(station == null) {
            throw new ObjectNotFoundException(TmsStation.class, id);
        }

        return tmsStationMetadata2FeatureConverter.convert(station);

    }
}
