package fi.livi.digitraffic.tie.service.jms.marshaller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.ely.lotju.kamera.proto.KuvaProtos;

public class WeathercamDataJMSMessageMarshaller extends BytesJMSMessageMarshaller<KuvaProtos.Kuva> {
    private static final Logger log = LoggerFactory.getLogger(WeathercamDataJMSMessageMarshaller.class);

    public List<KuvaProtos.Kuva> getObjectFromBytes(final byte[] bytes) {
        final List<KuvaProtos.Kuva> kuvaList = new ArrayList<>();

        try {
            final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

            while (bais.available() > 0) {
                final KuvaProtos.Kuva kuva = KuvaProtos.Kuva.parseDelimitedFrom(bais);

                kuvaList.add(kuva);
            }
        } catch (final IOException e) {
            log.error("Exception while parsing", e);
        }

        return kuvaList;
    }
}
