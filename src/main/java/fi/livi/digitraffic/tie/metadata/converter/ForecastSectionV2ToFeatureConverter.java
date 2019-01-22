package fi.livi.digitraffic.tie.metadata.converter;

import java.util.List;

import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.metadata.geojson.MultiLineString;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionV2Feature;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionV2Properties;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSection;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSectionCoordinateList;

@Component
public class ForecastSectionV2ToFeatureConverter {

    public static ForecastSectionV2Feature convert(final ForecastSection forecastSection) {
        return new ForecastSectionV2Feature(forecastSection.getId(), getGeometry(forecastSection.getForecastSectionCoordinateLists()),
                                            getProperties(forecastSection));
    }

    private static ForecastSectionV2Properties getProperties(final ForecastSection forecastSection) {
        return new ForecastSectionV2Properties(forecastSection.getNaturalId(), forecastSection.getDescription(),
                                               forecastSection.getRoadNumber(), forecastSection.getRoadSectionNumber(), forecastSection.getLength());
    }

    private static MultiLineString getGeometry(final List<ForecastSectionCoordinateList> forecastSectionCoordinateLists) {
        final MultiLineString multiLineString = new MultiLineString();
        for (final ForecastSectionCoordinateList list : forecastSectionCoordinateLists) {
            multiLineString.addLineString(list.getListCoordinates());
        }
        return multiLineString;
    }
}
