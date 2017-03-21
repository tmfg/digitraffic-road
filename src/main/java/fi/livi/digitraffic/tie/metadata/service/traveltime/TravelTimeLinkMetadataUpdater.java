package fi.livi.digitraffic.tie.metadata.service.traveltime;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.service.traveltime.TravelTimeClient;
import fi.livi.digitraffic.tie.metadata.dao.LinkRepository;
import fi.livi.digitraffic.tie.metadata.model.Link;
import fi.livi.digitraffic.tie.metadata.service.traveltime.dto.LinkDto;
import fi.livi.digitraffic.tie.metadata.service.traveltime.dto.LinkMetadataDto;
import fi.livi.digitraffic.tie.metadata.service.traveltime.dto.SiteDto;

@Service
public class TravelTimeLinkMetadataUpdater {

    private static final Logger log = LoggerFactory.getLogger(TravelTimeLinkMetadataUpdater.class);

    private final LinkRepository linkRepository;

    private final TravelTimeClient travelTimeClient;

    @Autowired
    public TravelTimeLinkMetadataUpdater(final LinkRepository linkRepository,
                                         final TravelTimeClient travelTimeClient) {
        this.linkRepository = linkRepository;
        this.travelTimeClient = travelTimeClient;
    }

    @Transactional
    public void updateLinkMetadata() {

        final LinkMetadataDto linkMetadata = travelTimeClient.getLinkMetadata();

        final Map<Long, Link> linksByNaturalId = linkRepository.findAll().stream().collect(Collectors.toMap(Link::getNaturalId, Function.identity()));

        linkRepository.makeNonObsoleteLinksObsolete();

        final List<LinkDto> linksToUpdate = linkMetadata.links.stream().filter(l -> linksByNaturalId.containsKey(l.linkNumber))
                                                                       .collect(Collectors.toList());

        // TODO: add missing links
        final List<LinkDto> linksToAdd = linkMetadata.links.stream().filter(l -> !linksByNaturalId.containsKey(l.linkNumber))
                                                                    .collect(Collectors.toList());

        final Map<Integer, SiteDto> sitesBySiteNumber = linkMetadata.sites.stream().collect(Collectors.toMap(s -> s.number, Function.identity()));

        updateLinks(linksByNaturalId, linksToUpdate, sitesBySiteNumber);
    }

    private void updateLinks(final Map<Long, Link> linksByNaturalId, final List<LinkDto> linksToUpdate, final Map<Integer, SiteDto> sitesBySiteNumber) {

        for (LinkDto linkData : linksToUpdate) {

            final Link link = linksByNaturalId.get(linkData.linkNumber);

            final long length = linkData.distance.unit.equals("km") ? linkData.distance.value.multiply(new BigDecimal(1000))
                                                                                             .round(MathContext.DECIMAL32).longValue()
                                                                    : linkData.distance.value.round(MathContext.DECIMAL32).longValue();

            final String name = linkData.names.stream().filter(n -> n.language.equals("fi")).map(n -> n.text).findFirst().orElse(link.getName());
            // TODO: setNameSv, setNameEn
            // TODO: link.setSummerFreeFlowSpeed();
            // TODO: link.setWinterFreeFlowSpeed();

            final SiteDto startSite = sitesBySiteNumber.get(linkData.startSite);
            final SiteDto endSite = sitesBySiteNumber.get(linkData.endSite);

            final Pattern p = Pattern.compile("(\\d+)/(\\d+)-(\\d+)");
            final Matcher m1 = p.matcher(startSite.roadRegisterAddress);
            final Matcher m2 = p.matcher(endSite.roadRegisterAddress);

            if (m1.matches() && m2.matches()) {

                final int special = startSite.roadNumber == endSite.roadNumber ? 0 : 1; // 1 if the link takes a turn

                final int startRoadSectionNumber = Integer.parseInt(m1.group(2));
                final int startRoadAddressDistance = Integer.parseInt(m1.group(3));
                final int endRoadSectionNumber = Integer.parseInt(m2.group(2));
                final int endRoadAddressDistance = Integer.parseInt(m2.group(3));

                int direction = 1;
                if (startRoadSectionNumber > endRoadSectionNumber ||
                    (startRoadSectionNumber == endRoadSectionNumber && startRoadAddressDistance > endRoadAddressDistance)) {
                    direction = 2;
                }

                log.info("Updating link (naturalId): {} with values: startSite.roadNumber: {}, startRoadSectionNumber: {}, endSite.roadNumber: {}, " +
                         "endRoadSectionNumber: {}, name: {}, length: {}, direction: {}, startRoadAddressDistance: {}, endRoadAddressDistance: {}, " +
                         "special: {}, obsolete: false, obsoleteDate: null",
                         link.getNaturalId(), startSite.roadNumber, startRoadSectionNumber, endSite.roadNumber, endRoadSectionNumber, name, length,
                         direction, startRoadAddressDistance, endRoadAddressDistance, special);

                linkRepository.updateLink(startSite.roadNumber, startRoadSectionNumber, endSite.roadNumber, endRoadSectionNumber, name, length,
                                          direction, startRoadAddressDistance, endRoadAddressDistance, special, link.getNaturalId());
            } else {
                log.error("Skipping link with invalid road address. Link naturalId: {}, startSite: {}, endSite: {}",
                         link.getNaturalId(), startSite.toString(), endSite.toString());
            }

        }

    }
}
