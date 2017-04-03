package fi.livi.digitraffic.tie.metadata.converter;

import java.time.ZonedDateTime;
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

        final LinkFeatureCollection linkFeatureCollection = new LinkFeatureCollection(lastUpdated);

        for (final Link link : links) {
            linkFeatureCollection.add(convert(link));
        }
        return linkFeatureCollection;
    }

    private LinkFeature convert(final Link link) {

        LinkFeature linkFeature = new LinkFeature();
        linkFeature.setId(link.getNaturalId());
        final List<Site> sites = link.getLinkSites().stream().sorted(Comparator.comparing(LinkSite::getOrderNumber))
                                                             .map(LinkSite::getSite).collect(Collectors.toList());
        linkFeature.setGeometry(getGeometry(sites));

        linkFeature.setProperties(new LinkProperties(link.getNaturalId(), sites, link.getName(), link.getNameSv(), link.getNameEn(), link.getLength(),
                                                     link.getStartRoadAddressDistance(), link.getEndRoadAddressDistance(),
                                                     link.getSummerFreeFlowSpeed(), link.getWinterFreeFlowSpeed(), link.getRoadDistrict(),
                                                     link.getLinkDirection()));
        return linkFeature;
    }

    private LineString getGeometry(final List<Site> linkSites) {

        final LineString geometry = new LineString();
        for (final Site site : linkSites) {
            geometry.addCoordinate(site.getLongitudeWgs84(), site.getLatitudeWgs84());
        }
        return geometry;
    }
}
