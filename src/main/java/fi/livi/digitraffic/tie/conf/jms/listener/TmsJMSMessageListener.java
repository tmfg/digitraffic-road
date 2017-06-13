package fi.livi.digitraffic.tie.conf.jms.listener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;

import com.google.protobuf.InvalidProtocolBufferException;
import fi.ely.lotju.lam.proto.LAMRealtimeProtos;

/**
 * Luotu: 13.6.2017 klo 11.22
 *
 * @author teijoro
 */
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

                debug(lam);

                lamList.add(lam);
            }
        } catch (final InvalidProtocolBufferException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return lamList;
    }

    private static void debug(final LAMRealtimeProtos.Lam lam) {
        System.out.println(String.format("%d : %d anturia", lam.getAsemaId(), lam.getAnturiCount()));
    }
}
