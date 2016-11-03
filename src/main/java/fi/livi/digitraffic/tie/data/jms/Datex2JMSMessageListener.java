package fi.livi.digitraffic.tie.data.jms;

import java.util.List;

import javax.jms.JMSException;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.data.service.Datex2DataService;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;

@ConditionalOnProperty(name = "jms.datex2.enabled")
@Component
public class Datex2JMSMessageListener extends AbstractJMSMessageListener<D2LogicalModel> {

    private static final Logger log = LoggerFactory.getLogger(Datex2JMSMessageListener.class);

    private final Datex2DataService datex2DataService;

    @Autowired
    public Datex2JMSMessageListener(final Datex2DataService datex2DataService) throws JMSException, JAXBException {
        super(D2LogicalModel.class, log);
        this.datex2DataService = datex2DataService;
    }

    @Override
    protected void handleData(List<Pair<D2LogicalModel, String>> data) {
        datex2DataService.updateDatex2Data(data);
    }
}
