package fi.livi.digitraffic.tie.conf.jms.listener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;

import fi.ely.lotju.lam.proto.LAMRealtimeProtos;

public class TmsJMSMessageListener extends NormalJMSMessageListener<LAMRealtimeProtos.Lam> {
    public TmsJMSMessageListener(final JMSDataUpdater<LAMRealtimeProtos.Lam> handleData, final boolean queueTopic, final Logger log) throws JAXBException {
        super(LAMRealtimeProtos.Lam.class, handleData, queueTopic, log);
    }

    @Override
    protected List<LAMRealtimeProtos.Lam> getObjectFromBytes(final byte[] body) {
        final List<LAMRealtimeProtos.Lam> lamList = new ArrayList<>();

        try {
            final ByteArrayInputStream bais = new ByteArrayInputStream(body);

            while(bais.available() > 0) {
                final LAMRealtimeProtos.Lam lam = LAMRealtimeProtos.Lam.parseDelimitedFrom(bais);

                lamList.add(lam);
            }
        } catch (final IOException e) {
            log.error("Exception while parsing", e);
        }

        return lamList;
    }
}
