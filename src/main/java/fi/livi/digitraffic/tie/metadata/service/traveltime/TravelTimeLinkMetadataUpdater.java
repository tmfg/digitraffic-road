package fi.livi.digitraffic.tie.metadata.service.traveltime;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.service.traveltime.TravelTimeClient;
import fi.livi.digitraffic.tie.metadata.dao.DirectionDao;
import fi.livi.digitraffic.tie.metadata.dao.LinkDao;
import fi.livi.digitraffic.tie.metadata.dao.LinkRepository;
import fi.livi.digitraffic.tie.metadata.dao.SiteDao;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;
import fi.livi.digitraffic.tie.metadata.service.traveltime.dto.DirectionTextDto;
import fi.livi.digitraffic.tie.metadata.service.traveltime.dto.LinkDto;
import fi.livi.digitraffic.tie.metadata.service.traveltime.dto.LinkMetadataDto;
import fi.livi.digitraffic.tie.metadata.service.traveltime.dto.NameDto;
import fi.livi.digitraffic.tie.metadata.service.traveltime.dto.SiteDto;
import fi.livi.digitraffic.tie.metadata.service.traveltime.dto.TextDto;

@Service
public class TravelTimeLinkMetadataUpdater {

    private static final Logger log = LoggerFactory.getLogger(TravelTimeLinkMetadataUpdater.class);

    private final LinkRepository linkRepository;
    private final LinkDao linkDao;
    private final SiteDao siteDao;
    private final DirectionDao directionDao;
    private final TravelTimeClient travelTimeClient;
    private final StaticDataStatusService staticDataStatusService;

    private final static Pattern roadAddressPattern = Pattern.compile("^(\\d+)\\/(\\d+)-(\\d+)$");

    @Autowired
    public TravelTimeLinkMetadataUpdater(final LinkRepository linkRepository,
                                         final LinkDao linkDao,
                                         final SiteDao siteDao,
                                         final DirectionDao directionDao,
                                         final TravelTimeClient travelTimeClient,
                                         final StaticDataStatusService staticDataStatusService) {
        this.linkRepository = linkRepository;
        this.linkDao = linkDao;
        this.siteDao = siteDao;
        this.directionDao = directionDao;
        this.travelTimeClient = travelTimeClient;
        this.staticDataStatusService = staticDataStatusService;
    }

    @Transactional
    public void updateLinkMetadataIfUpdateAvailable() {

        final MetadataUpdated updated = staticDataStatusService.findMetadataUpdatedByMetadataType(MetadataType.TRAVEL_TIME_LINKS);
        final LinkMetadataDto linkMetadata = travelTimeClient.getLinkMetadata();

        if (updated == null || updated.getUpdatedTime().isBefore(linkMetadata.lastUpdate)) {
            updateLinkMetadata(linkMetadata);
            staticDataStatusService.setMetadataUpdated(MetadataType.TRAVEL_TIME_LINKS, linkMetadata.lastUpdate);
        }
    }

    @Transactional
    protected void updateLinkMetadata(final LinkMetadataDto linkMetadata) {

        final Map<Integer, SiteDto> sitesBySiteNumber = linkMetadata.sites.stream().collect(Collectors.toMap(s -> s.number, Function.identity()));

        siteDao.makeNonObsoleteSitesObsolete();
        createOrUpdateSites(linkMetadata.sites);

        directionDao.makeNonObsoleteDirectionsObsolete();
        createOrUpdateDirections(linkMetadata.directions);

        linkRepository.makeNonObsoleteLinksObsolete();
        createOrUpdateLinks(linkMetadata.links, sitesBySiteNumber);
    }

    private void createOrUpdateSites(final List<SiteDto> sites) {

        for (final SiteDto site : sites) {

            final Pair<Double, Double> coordinatesWgs84 = getCoordinatesWgs84(site.coordinatesKkj3.x, site.coordinatesKkj3.y);
            final Integer roadSectionNumber = getRoadSectionNumber(site.roadRegisterAddress);

            if (roadSectionNumber != null) {
                siteDao.createOrUpdateSite(site.number,
                                           getName(site.names, "fi", SiteDto.class, site.number),
                                           getName(site.names, "sv", SiteDto.class, site.number),
                                           getName(site.names, "en", SiteDto.class, site.number),
                                           site.roadNumber, roadSectionNumber, site.coordinatesKkj3.x, site.coordinatesKkj3.y,
                                           coordinatesWgs84.getLeft(), coordinatesWgs84.getRight());
            } else {
                log.error("Skipping Site with natural id {}. Could not parse roadSectionNumber from roadRegisterAddress {}", site.number, site.roadRegisterAddress);
            }
        }
    }

    private void createOrUpdateDirections(final List<DirectionTextDto> directions) {

        for (final DirectionTextDto direction : directions) {

            List<TextDto> directionNames = Stream.of("fi", "sv", "en")
                .map(lang -> direction.texts.stream().filter(d -> d.lang.equals(lang)).findFirst().orElse(new TextDto("", "")))
                .collect(Collectors.toList());

            directionDao.createOrUpdateDirection(direction.directionIndex, directionNames.get(0).text, directionNames.get(1).text,
                                                 directionNames.get(2).text, direction.roadDirection);
        }
    }

    private void createOrUpdateLinks(final List<LinkDto> links, final Map<Integer, SiteDto> sitesBySiteNumber) {

        for (final LinkDto linkData : links) {

            final long length = linkData.distance.unit.equals("km") ? linkData.distance.value.multiply(new BigDecimal(1000))
                                                                                             .round(MathContext.DECIMAL32).longValue()
                                                                    : linkData.distance.value.round(MathContext.DECIMAL32).longValue();

            final SiteDto startSite = sitesBySiteNumber.get(linkData.startSite);
            final SiteDto endSite = sitesBySiteNumber.get(linkData.endSite);

            final Matcher m1 = roadAddressPattern.matcher(startSite.roadRegisterAddress);
            final Matcher m2 = roadAddressPattern.matcher(endSite.roadRegisterAddress);

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

                log.info("Creating or updating link (naturalId): {} with values: startSite.roadNumber: {}, startRoadSectionNumber: {}, endSite.roadNumber: {}, " +
                         "endRoadSectionNumber: {}, name: {}, length: {}, direction: {}, startRoadAddressDistance: {}, endRoadAddressDistance: {}, " +
                         "special: {}, obsolete: false, obsoleteDate: null",
                         linkData.linkNumber, startSite.roadNumber, startRoadSectionNumber, endSite.roadNumber, endRoadSectionNumber,
                         getName(linkData.names, "fi", LinkDto.class, linkData.linkNumber), length, direction,
                         startRoadAddressDistance, endRoadAddressDistance, special);

                // TODO: summerFreeFlowSpeed, winterFreeFlowSpeed
                linkDao.createOrUpdateLink(startSite.roadNumber, startRoadSectionNumber, endSite.roadNumber, endRoadSectionNumber,
                                           getName(linkData.names, "fi", LinkDto.class, linkData.linkNumber),
                                           getName(linkData.names, "sv", LinkDto.class, linkData.linkNumber),
                                           getName(linkData.names, "en", LinkDto.class, linkData.linkNumber),
                                           length, direction, startRoadAddressDistance, endRoadAddressDistance, special, linkData.linkNumber);

                final List<Integer> siteNaturalIds = new ArrayList<>();
                siteNaturalIds.add(linkData.startSite);
                if (linkData.intermediates != null) {
                    siteNaturalIds.addAll(linkData.intermediates.stream().sorted(Comparator.comparingInt(s -> s.index)).map(s -> s.number).collect(Collectors.toList()));
                }
                siteNaturalIds.add(linkData.endSite);

                linkDao.createOrUpdateLinkSites(linkData.linkNumber, siteNaturalIds);

                linkDao.createOrUpdateLinkDirection(linkData.linkNumber, linkData.directionIndex);
            } else {
                log.error("Skipping link with invalid road address. Link naturalId: {}, startSite: {}, endSite: {}",
                          linkData.linkNumber, startSite.toString(), endSite.toString());
            }
        }
    }

    private static String getName(final List<NameDto> names, final String language, final Class clazz, final Number id) {
        final Optional<String> name = names.stream().filter(n -> n.language.equals(language)).map(n -> n.text).findFirst();
        if (!name.isPresent()) {
            log.warn("{} with naturalId {} is missing name in {} language", clazz.getSimpleName(), id, language);
            return "";
        }
        return name.get();
    }

    private static Integer getRoadSectionNumber(final String roadRegisterAddress) {
        final Matcher m = roadAddressPattern.matcher(roadRegisterAddress);
        if (m.matches()) {
            return Integer.parseInt(m.group(2));
        }
        return null;
    }

    private static Pair<Double, Double> getCoordinatesWgs84(final Long x, final Long y) {
        if (x != null && y != null) {
            final Point point = CoordinateConverter.convertFromKKJ3ToWGS84(new Point(x, y));
            return Pair.of(point.getLongitude(), point.getLatitude());
        }
        return Pair.of(null, null);
    }
}
