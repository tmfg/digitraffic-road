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
        final ObservationFeature tgt = new ObservationFeature();

        if (src.getSijainti() != null && src.getSijainti().getKoordinaatit() != null) {
            KoordinaattisijaintiSchema coords = src.getSijainti().getKoordinaatit();
            final Point point =
                CoordinateConverter.convertFromETRS89ToWGS84(
                    new Point(coords.getX(), coords.getY(), coords.getZ()));
            tgt.setGeometry(point);
        }
        tgt.setProperties(conversionService.convert(src, ObservationProperties.class));
        return tgt;
    }
}
