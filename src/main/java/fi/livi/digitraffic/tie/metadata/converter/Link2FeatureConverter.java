package fi.livi.digitraffic.tie.metadata.converter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.metadata.geojson.LineString;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.metadata.geojson.traveltime.LinkFeature;
import fi.livi.digitraffic.tie.metadata.geojson.traveltime.LinkFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.traveltime.LinkProperties;
import fi.livi.digitraffic.tie.metadata.model.Link;
import fi.livi.digitraffic.tie.metadata.model.LinkSite;
import fi.livi.digitraffic.tie.metadata.model.Site;

@Component
public class Link2FeatureConverter extends AbstractMetadataToFeatureConverter {

    protected Link2FeatureConverter(CoordinateConverter coordinateConverter) {
        super(coordinateConverter);
    }

    public LinkFeatureCollection convert(final List<Link> links, final ZonedDateTime lastUpdated) {

        final ArrayList<LinkFeature> linkFeatures = new ArrayList<>();

        for (final Link link : links) {
            linkFeatures.add(convert(link));
        }

        return new LinkFeatureCollection(lastUpdated, linkFeatures);
    }

    private LinkFeature convert(final Link link) {

        final List<Site> sites = link.getLinkSites().stream().sorted(Comparator.comparing(LinkSite::getOrderNumber))
                                                             .map(LinkSite::getSite).collect(Collectors.toList());

        return new LinkFeature(link.getNaturalId(), getGeometry(sites),
                               new LinkProperties(link.getNaturalId(), sites, link.getName(), link.getNameSv(), link.getNameEn(), link.getLength(),
                                                  link.getSummerFreeFlowSpeed(), link.getWinterFreeFlowSpeed(), link.getRoadDistrict(),
                                                  link.getLinkDirection()));
    }

    private LineString getGeometry(final List<Site> linkSites) {

        return new LineString(linkSites.stream().map(s -> Arrays.asList(s.getLongitudeWgs84(), s.getLatitudeWgs84())).collect(Collectors.toList()));
    }
}
