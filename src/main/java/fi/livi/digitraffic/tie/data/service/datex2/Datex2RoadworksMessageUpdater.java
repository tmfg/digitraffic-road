package fi.livi.digitraffic.tie.data.service.datex2;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.Datex2Repository;
import fi.livi.digitraffic.tie.data.service.Datex2UpdateService;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.ObjectFactory;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.Situation;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.SituationPublication;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.SituationRecord;

@Service
public class Datex2RoadworksMessageUpdater {
    private final Datex2RoadworksHttpClient datex2RoadworksHttpClient;
    private final Datex2UpdateService datex2UpdateService;
    private final Datex2Repository datex2Repository;

    private final StringToObjectMarshaller stringToObjectMarshaller;

    private static final Logger log = LoggerFactory.getLogger(Datex2RoadworksMessageUpdater.class);

    @Autowired
    public Datex2RoadworksMessageUpdater(final Datex2RoadworksHttpClient datex2RoadworksHttpClient, final Datex2UpdateService datex2UpdateService,
        final Datex2Repository datex2Repository, final StringToObjectMarshaller stringToObjectMarshaller) {
        this.datex2RoadworksHttpClient = datex2RoadworksHttpClient;
        this.datex2UpdateService = datex2UpdateService;
        this.datex2Repository = datex2Repository;
        this.stringToObjectMarshaller = stringToObjectMarshaller;
    }

    @Transactional
    public void updateDatex2RoadworksMessages() {
        final String message = datex2RoadworksHttpClient.getRoadWorksMessage();

        datex2UpdateService.updateRoadworks(convert(message));
    }

    private List<Datex2MessageDto> convert(final String message) {
        final D2LogicalModel model = stringToObjectMarshaller.convertToObject(message);

        return createModels(model);
    }

    private List<Datex2MessageDto> createModels(final D2LogicalModel main) {
        final SituationPublication sp = (SituationPublication) main.getPayloadPublication();

        final Map<String, LocalDateTime> versionTimes = datex2UpdateService.listRoadworkSituationVersionTimes();
        final long updatedCount = sp.getSituation().stream().filter(s -> isNewSituation(versionTimes.get(s.getId()), s)).count();
        final long newCount = sp.getSituation().stream().filter(s -> versionTimes.get(s.getId()) == null).count();

        log.info("situations.updated={}, situations.new={}", updatedCount, newCount);

        return sp.getSituation().stream()
            .filter(s -> isNewSituation(versionTimes.get(s.getId()), s))
            .map(s -> convert(main, sp, s))
            .collect(Collectors.toList());
    }

    private static boolean isNewSituation(final LocalDateTime latestVersionTime, final Situation situation) {
        // does any record have new version time?
        return situation.getSituationRecord().stream().anyMatch(r -> isNewRecord(latestVersionTime, r));
    }

    private static boolean isNewRecord(final LocalDateTime latestVersionTime, final SituationRecord record) {
        // different resolution, so remove fractions of second
        final LocalDateTime vTime = record.getSituationRecordVersionTime().toGregorianCalendar().toZonedDateTime().toLocalDateTime().withNano(0);

        return latestVersionTime == null || vTime.isAfter(latestVersionTime);
    }

    private Datex2MessageDto convert(final D2LogicalModel main, final SituationPublication sp, final Situation situation) {
        final D2LogicalModel d2 = new D2LogicalModel();
        final SituationPublication newSp = new SituationPublication();

        newSp.setPublicationTime(sp.getPublicationTime());
        newSp.setPublicationCreator(sp.getPublicationCreator());
        newSp.setLang(sp.getLang());
        newSp.withSituation(situation);

        d2.setModelBaseVersion(main.getModelBaseVersion());
        d2.setExchange(main.getExchange());
        d2.setPayloadPublication(newSp);

        final JAXBElement<D2LogicalModel> element = new ObjectFactory().createD2LogicalModel(d2);
        return new Datex2MessageDto(stringToObjectMarshaller.convertToString(element), null, d2);
    }
}
