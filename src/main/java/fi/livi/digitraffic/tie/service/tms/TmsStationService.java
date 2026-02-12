package fi.livi.digitraffic.tie.service.tms;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.tms.TmsStationRepository;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAsemaVO;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.roadstation.RoadStation;
import fi.livi.digitraffic.tie.model.tms.CalculatorDeviceType;
import fi.livi.digitraffic.tie.model.tms.TmsStation;
import fi.livi.digitraffic.tie.model.tms.TmsStationType;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.service.UpdateStatus;

@Service
public class TmsStationService extends AbstractTmsStationAttributeUpdater {
    private static final Logger log = LoggerFactory.getLogger(TmsStationService.class);

    private final TmsStationRepository tmsStationRepository;

    @Autowired
    public TmsStationService(final TmsStationRepository tmsStationRepository) {
        this.tmsStationRepository = tmsStationRepository;
    }

    @Transactional
    public List<TmsStation> findAllPublishableTmsStations() {
        return tmsStationRepository.findByRoadStationPublishableIsTrueOrderByRoadStation_NaturalId();
    }

    @Transactional(readOnly = true)
    public Map<Long, TmsStation> findAllTmsStationsMappedByByLotjuId() {
        final List<TmsStation> allStations = tmsStationRepository.findAll();

        return allStations.stream().collect(Collectors.toMap(TmsStation::getLotjuId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public List<Long> findAllTmsStationsLotjuIds() {
        return tmsStationRepository.findAllTmsStationsLotjuIds();
    }

    @Transactional(readOnly = true)
    public Map<Long, TmsStation> findAllPublishableTmsStationsMappedByLotjuId() {
        final List<TmsStation> all = findAllPublishableTmsStations();
        return all.stream().collect(Collectors.toMap(TmsStation::getLotjuId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public TmsStation findPublishableTmsStationByRoadStationNaturalId(final long roadStationNaturalId) {
        final TmsStation entity = tmsStationRepository.findByRoadStationPublishableIsTrueAndRoadStation_NaturalId(roadStationNaturalId);

        if (entity == null) {
            throw new ObjectNotFoundException(TmsStation.class, roadStationNaturalId);
        }

        return entity;
    }

    @Transactional
    public UpdateStatus updateOrInsertTmsStation(final LamAsemaVO lam) {
        final TmsStation existingTms = findTmsStationByLotjuId(lam.getId());

        if (existingTms != null) {
            final int hash = HashCodeBuilder.reflectionHashCode(existingTms);
            final String before = ToStringHelper.toStringFull(existingTms);

            if ( updateTmsStationAttributes(lam, existingTms) ||
                hash != HashCodeBuilder.reflectionHashCode(existingTms) ) {
                log.info("method=updateOrInsertTmsStation Updated:\n{} ->\n{}", before, ToStringHelper.toStringFull(existingTms));
                return UpdateStatus.UPDATED;
            }
            return UpdateStatus.NOT_UPDATED;
        } else {
            final TmsStation newTms = TmsStation.create();
            updateTmsStationAttributes(lam, newTms);
            tmsStationRepository.save(newTms);

            log.info("method=updateOrInsertTmsStation Created new {}", newTms);
            return UpdateStatus.INSERTED;
        }
    }

    @Override
    @Transactional
    public boolean updateStationToObsoleteWithLotjuId(final long lotjuId) {
        final TmsStation station = tmsStationRepository.findByLotjuId(lotjuId);
        if (station != null) {
            return station.makeObsolete();
        }
        return false;
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

        // Update RoadStation
        final boolean updated = updateRoadStationAttributes(from, to.getRoadStation());
        to.setObsoleteDate(to.getRoadStation().getObsoleteDate());

        return updated ||
            hash != HashCodeBuilder.reflectionHashCode(to);
    }

    @Transactional
    public TmsStation findTmsStationByLotjuId(final Long lotjuId) {
        return tmsStationRepository.findByLotjuId(lotjuId);
    }

    private static Long convertToTmsNaturalId(final Integer roadStationVanhaId) {
        return roadStationVanhaId == null ?
               null :
               roadStationVanhaId > 23000 ? roadStationVanhaId - 23000L : roadStationVanhaId;
    }

}
