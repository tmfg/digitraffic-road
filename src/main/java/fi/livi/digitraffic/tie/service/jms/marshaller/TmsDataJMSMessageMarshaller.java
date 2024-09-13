package fi.livi.digitraffic.tie.service.jms.marshaller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.ely.lotju.lam.proto.LAMRealtimeProtos;

public class TmsDataJMSMessageMarshaller extends BytesJMSMessageMarshaller<LAMRealtimeProtos.Lam> {
    private static final Logger log = LoggerFactory.getLogger(TmsDataJMSMessageMarshaller.class);

    public List<LAMRealtimeProtos.Lam> getObjectFromBytes(final byte[] body) {
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
