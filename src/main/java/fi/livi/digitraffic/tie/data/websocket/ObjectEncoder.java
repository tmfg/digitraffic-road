package fi.livi.digitraffic.tie.data.websocket;

import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class ObjectEncoder<T> implements Encoder.Text<T> {
    private static final Logger log = LoggerFactory.getLogger(ObjectEncoder.class);

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
                                                          .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    @Override
    public void init(final EndpointConfig config) {
        // no need
    }

    @Override
    public String encode(final T message) {
        try {
            return mapper.writeValueAsString(message);
        } catch (final JsonProcessingException e) {
            log.error("Error when encoding message " + message, e);
            return "";
        }
    }

    @Override
    public void destroy() {
        // no need
    }

}
