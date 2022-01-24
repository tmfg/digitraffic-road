package fi.livi.digitraffic.tie.converter;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeatureCollection;

public class SimpleTrafficMessageConverter extends JsonSerializer<TrafficAnnouncementFeatureCollection> {
    @Override
    public void serialize(final TrafficAnnouncementFeatureCollection value, final JsonGenerator gen,
                          final SerializerProvider serializers) throws IOException {
        serializers.defaultSerializeValue(value, gen);
    }


}
