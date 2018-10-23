package fi.livi.digitraffic.tie.data.model.maintenance.converter;

import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.data.model.maintenance.ObservationFeature;
import fi.livi.digitraffic.tie.data.model.maintenance.ObservationProperties;
import fi.livi.digitraffic.tie.harja.Havainto;
import fi.livi.digitraffic.tie.harja.entities.KoordinaattisijaintiSchema;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;

@Component
public class HavaintoToObservationFeatureConverter extends AutoRegisteredConverter<Havainto, ObservationFeature> {

    @Override
    public ObservationFeature convert(final Havainto src) {

        Point point = resolveGeometry(src);

        final ObservationFeature tgt =
            new ObservationFeature(point,
                                   convert(src, ObservationProperties.class));
        return tgt;
    }

    private Point resolveGeometry(Havainto src) {
        if (src.getSijainti() != null && src.getSijainti().getKoordinaatit() != null) {
            final KoordinaattisijaintiSchema coords = src.getSijainti().getKoordinaatit();
            return CoordinateConverter.convertFromETRS89ToWGS84(
                new Point(coords.getX(), coords.getY(), coords.getZ()));
        }
        return null;
    }
}
