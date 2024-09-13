package fi.livi.digitraffic.tie.service.jms.marshaller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.ely.lotju.tiesaa.proto.TiesaaProtos;

public class WeatherDataJMSMessageMarshaller extends BytesJMSMessageMarshaller<TiesaaProtos.TiesaaMittatieto> {
    private static final Logger log = LoggerFactory.getLogger(WeatherDataJMSMessageMarshaller.class);

    public List<TiesaaProtos.TiesaaMittatieto> getObjectFromBytes(final byte[] bytes) {
        final List<TiesaaProtos.TiesaaMittatieto> weatherList = new ArrayList<>();

        try {
            final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

            while (bais.available() > 0) {
                final TiesaaProtos.TiesaaMittatieto weather = TiesaaProtos.TiesaaMittatieto.parseDelimitedFrom(bais);

                weatherList.add(weather);
            }
        } catch (final IOException e) {
            log.error("Exception while parsing", e);
        }

        return weatherList;
    }
}
