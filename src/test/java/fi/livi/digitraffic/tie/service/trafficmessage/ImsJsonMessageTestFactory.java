package fi.livi.digitraffic.tie.service.trafficmessage;

import static fi.livi.digitraffic.tie.TestUtils.readResourceContent;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectReader;

import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.Area;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.AreaLocation;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.Feature;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.ItineraryLeg;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.ItineraryRoadLeg;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.LastActiveItinerarySegment;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.Restriction;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.RoadWorkPhase;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.TrafficAnnouncement;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.TrafficAnnouncementProperties;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.WeekdayTimePeriod;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.Worktype;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;

public class ImsJsonMessageTestFactory {

    public static final String MAX_DURATION = "PT8H";
    public static final String MIN_DURATION = "PT6H";
    public static final String WORK_PHASE_ID = "WP1";

    public static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.ImsGeoJsonFeature createTrafficAnnouncementJsonMessage(
        final TrafficAnnouncementProperties.SituationType situationType,
        final boolean areaLocation,
        final ObjectReader readerForGeometry) throws IOException {
        return createJsonMessage(situationType, TrafficAnnouncementProperties.TrafficAnnouncementType.GENERAL, areaLocation, ZonedDateTime.now(),
            fi.livi.digitraffic.tie.external.tloik.ims.jmessage.Restriction.Type.NARROW_LANES,
            "Nopeusrajoitus", 40.0, "km/h",
            fi.livi.digitraffic.tie.external.tloik.ims.jmessage.Worktype.Type.BRIDGE,
            "tloik/ims/regions/00073_Helsinki.json",
            readerForGeometry);
    }

    public static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.ImsGeoJsonFeature createJsonMessage(
            final TrafficAnnouncementProperties.SituationType situationType,
            final TrafficAnnouncementProperties.TrafficAnnouncementType trafficAnnouncementType,
            final boolean createWithAreaLocation,
            final ZonedDateTime releaseTime, final Restriction.Type restrictionType, final String restrictionName, final Double restrictionQuantity, final String restrictionUnit,
            final Worktype.Type worktype,
            final String geometryPath,
            final ObjectReader readerForGeometry)
        throws IOException {

        final String geometryJson = readResourceContent("classpath:" + geometryPath);
        final Geometry<?> geometry = readerForGeometry.readValue(geometryJson);
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.TrafficAnnouncementProperties
            properties = new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.TrafficAnnouncementProperties()
            .withVersion(1)
            .withSituationId("GUID123456")
            .withReleaseTime(releaseTime)
            .withSituationType(situationType)
            .withTrafficAnnouncementType(trafficAnnouncementType)
            .withAnnouncements(Collections.singletonList(
                new TrafficAnnouncement()
                    .withLanguage(TrafficAnnouncement.Language.FI)
                    .withTitle("Title")
                    .withLocation(createLocation())
                    .withLocationDetails(createLocationDetails(createWithAreaLocation))
                    // V0.2.5 Features
                    // V0.2.14 Features timeAndDuration
                    .withFeatures(Collections.singletonList(new Feature(restrictionName, restrictionQuantity, restrictionUnit,
                                                              "Some nice description!", createTimeAndDuration(releaseTime))))
                    .withComment("TEST")
                    .withTimeAndDuration(createTimeAndDuration(releaseTime))
                    .withAdditionalInformation("Liikenne- ja kelitiedot verkossa: http://liikennetilanne.tmfg.fi/")
                    .withSender("Tieliikennekeskus Helsinki")
                    .withRoadWorkPhases(situationType == TrafficAnnouncementProperties.SituationType.ROAD_WORK ?
                                        Collections.singletonList(createRoadWorkPhase(releaseTime, restrictionType, restrictionName, restrictionQuantity, restrictionUnit, worktype)) :
                                        Collections.emptyList())
                    // V0.2.8.
                    .withEarlyClosing(TrafficAnnouncement.EarlyClosing.CANCELED)
                    // V0.2.6
                    .withLastActiveItinerarySegment(
                        new LastActiveItinerarySegment()
                            .withEndTime(releaseTime)
                            .withStartTime(releaseTime.minusHours(1))
                            .withLegs(Collections.singletonList(
                                new ItineraryLeg()
                                    .withRoadLeg(
                                        new ItineraryRoadLeg()
                                            .withRoadName("Groom Lake Road")
                                            .withRoadNumber(123)
                                            .withEndArea("Area 51")
                                            .withStartArea("Area 52"))
                                    .withStreetName("Groom Lake Road"))))
            ))
            .withContact(new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.Contact("123456789", "helsinki@liikennekeskus.fi"));
        return new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.ImsGeoJsonFeature()
            .withType(fi.livi.digitraffic.tie.external.tloik.ims.jmessage.ImsGeoJsonFeature.Type.FEATURE)
            .withProperties(properties)
            .withGeometry(geometry);

    }

    private static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.TimeAndDuration createTimeAndDuration(final ZonedDateTime releaseTime) {
        return new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.TimeAndDuration(
            releaseTime, releaseTime.plusHours(2),
            new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.EstimatedDuration().withInformal("Yli 6 tuntia")
                .withMaximum(MAX_DURATION)
                .withMinimum(MIN_DURATION));
    }

    private static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.LocationDetails createLocationDetails(final boolean areaLocation) {
        return new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.LocationDetails()
            .withAreaLocation(areaLocation ? new AreaLocation().withAreas(Collections.singletonList(new Area("Helsinki", 73, Area.Type.MUNICIPALITY))) : null)
            .withRoadAddressLocation(areaLocation ? null :
                new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.RoadAddressLocation(
                    createRoadPoint(1), createRoadPoint(2), fi.livi.digitraffic.tie.external.tloik.ims.jmessage.RoadAddressLocation.Direction.POS, "Marjamäen suuntaan")
            );
    }

    private static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.RoadPoint createRoadPoint(final int id) {
        return new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.RoadPoint(
            "Lempäälä" + id, "Pirkanmaa", "Suomi",
            new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.RoadAddress(130, 24, 4000),
            "Tie 123", new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.AlertCLocation(37128, "Marjamäki", 2000));
    }

    private static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.Location createLocation() {
        return new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.Location(358, 10, "1.1.1", "Location description");
    }

    private static RoadWorkPhase createRoadWorkPhase(final ZonedDateTime releaseTime, final Restriction.Type restrictionType, final String featureName, final Double featureQuantity,
                                                     final String featureUnit, final Worktype.Type wokType) {
        return new RoadWorkPhase()
            .withComment("Työn aloitus")
            // V0.2.14 Features timeAndDuration
            .withRestrictions(Collections.singletonList(new Restriction(restrictionType, new Feature(featureName, featureQuantity, featureUnit,
                                                                                            "Some nice description!", createTimeAndDuration(releaseTime)))))
            // V0.2.13+ RestrictionsLiftable
            .withRestrictionsLiftable(true)
            .withWorktypes(Collections.singletonList(new Worktype(wokType, wokType.toString())))
            .withId(WORK_PHASE_ID)
            .withLocation(createLocation())
            .withLocationDetails(createLocationDetails(false))
            // V0.2.8.
            .withSeverity(RoadWorkPhase.Severity.HIGH)
            .withTimeAndDuration(createTimeAndDuration(releaseTime))
            .withWorkingHours(Arrays.asList(new WeekdayTimePeriod(WeekdayTimePeriod.Weekday.MONDAY, "8:00", "15:30"), new WeekdayTimePeriod(WeekdayTimePeriod.Weekday.TUESDAY, "8:00", "15:30")))
            .withSlowTrafficTimes(Arrays.asList(new WeekdayTimePeriod(WeekdayTimePeriod.Weekday.TUESDAY, "8:00", "15:30"), new WeekdayTimePeriod(WeekdayTimePeriod.Weekday.WEDNESDAY, "8:00", "15:30")))
            .withQueuingTrafficTimes(Arrays.asList(new WeekdayTimePeriod(WeekdayTimePeriod.Weekday.WEDNESDAY, "8:00", "15:30"), new WeekdayTimePeriod(WeekdayTimePeriod.Weekday.THURSDAY, "8:00", "15:30")));
    }
}
