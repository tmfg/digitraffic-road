package fi.livi.digitraffic.tie.metadata.service.traveltime;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.metadata.converter.Link2FeatureConverter;
import fi.livi.digitraffic.tie.metadata.dao.LinkRepository;
import fi.livi.digitraffic.tie.metadata.geojson.traveltime.LinkFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.Link;

@Service
public class TravelTimeLinkMetadataService {

    private final LinkRepository linkRepository;
    private final Link2FeatureConverter link2FeatureConverter;

    @Autowired
    public TravelTimeLinkMetadataService(final LinkRepository linkRepository,
                                         final Link2FeatureConverter link2FeatureConverter) {
        this.linkRepository = linkRepository;
        this.link2FeatureConverter = link2FeatureConverter;
    }

    public LinkFeatureCollection getLinkMetadata() {
        final List<Link> links = linkRepository.findByObsoleteDateOrderByNaturalId(null);

        final LinkFeatureCollection linkFeatureCollection = link2FeatureConverter.convert(links, ZonedDateTime.now());

        return linkFeatureCollection;
    }
}
