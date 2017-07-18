package fi.livi.digitraffic.tie.data.service.datex2;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.xml.transform.StringSource;

import fi.livi.digitraffic.tie.data.dao.Datex2Repository;
import fi.livi.digitraffic.tie.data.model.Datex2;
import fi.livi.digitraffic.tie.data.service.Datex2DataService;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;

@Service
public class Datex2MessageUpdater {

    private final Datex2Repository datex2Repository;
    private final Datex2HttpClient datex2HttpClient;
    private final Jaxb2Marshaller jaxb2Marshaller;
    private final Datex2DataService datex2DataService;

    @Autowired
    public Datex2MessageUpdater(final Datex2Repository datex2Repository, final Datex2HttpClient datex2HttpClient,
                                final Jaxb2Marshaller jaxb2Marshaller, final Datex2DataService datex2DataService) {
        this.datex2Repository = datex2Repository;
        this.datex2HttpClient = datex2HttpClient;
        this.jaxb2Marshaller = jaxb2Marshaller;
        this.datex2DataService = datex2DataService;
    }

    @Transactional
    public void updateDatex2Messages() {

        final Datex2 latest = datex2Repository.getLatest();

        final ZonedDateTime from = latest == null ? null : latest.getPublicationTime();

        final List<String> messages = datex2HttpClient.getDatex2MessagesFrom(from);

        final ArrayList<Pair<D2LogicalModel, String>> unmarshalled = new ArrayList<>();

        for (final String message : messages) {
            final D2LogicalModel d2 = unmarshal(message);
            unmarshalled.add(Pair.of(d2, message));
        }
        datex2DataService.updateDatex2Data(unmarshalled);
    }

    private D2LogicalModel unmarshal(final String message) {
        Object object = jaxb2Marshaller.unmarshal(new StringSource(message));
        if (object instanceof JAXBElement) {
            // For Datex2 messages extra stuff
            object = ((JAXBElement) object).getValue();
        }

        return (D2LogicalModel) object;
    }
}
