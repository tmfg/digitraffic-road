package fi.livi.digitraffic.tie.service.datex2;

import static fi.livi.digitraffic.tie.service.datex2.Datex2JsonConverterServiceTest.WORK_PHASE_ID;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectReader;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.Area;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.AreaLocation;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.Feature;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.ItineraryLeg;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.ItineraryRoadLeg;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.LastActiveItinerarySegment;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.Restriction;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.RoadWorkPhase;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.TrafficAnnouncement;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.TrafficAnnouncementProperties;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.WorkingHour;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.Worktype;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;

public class ImsJsonMessageFactory {

    public static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.ImsGeoJsonFeature createTrafficAnnouncementJsonMessage(
        final TrafficAnnouncementProperties.SituationType situationType,
        final boolean areaLocation,
        final ObjectReader readerForGeometry) throws IOException {
        return createJsonMessage(situationType, TrafficAnnouncementProperties.TrafficAnnouncementType.GENERAL, areaLocation, ZonedDateTime.now(),
            fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.Restriction.Type.NARROW_LANES,
            "Nopeusrajoitus", 40.0, "km/h",
            fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.Worktype.Type.BRIDGE,
            "tloik/ims/regions/00073_Helsinki.json",
            readerForGeometry);
    }

    public static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.ImsGeoJsonFeature createJsonMessage(
        final TrafficAnnouncementProperties.SituationType situationType,
        final TrafficAnnouncementProperties.TrafficAnnouncementType trafficAnnouncementType,
        final boolean createWithAreaLocation,
        final ZonedDateTime releaseTime, Restriction.Type restrictionType, final String restrictionName, final Double restrictionQuantity, final String restrictionUnit,
        final Worktype.Type worktype,
        final String geometryPath,
        final ObjectReader readerForGeometry)
        throws IOException {

        final String geometryJson = AbstractTest.readResourceContent("classpath:" + geometryPath);
        final Geometry<?> geometry = readerForGeometry.readValue(geometryJson);
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.TrafficAnnouncementProperties
            properties = new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.TrafficAnnouncementProperties()
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
                    // V0.2.5
                    .withFeatures(Collections.singletonList(new Feature(restrictionName, restrictionQuantity, restrictionUnit)))
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
            .withContact(new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.Contact("123456789", "helsinki@liikennekeskus.fi"));
        return new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.ImsGeoJsonFeature()
            .withType(fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.ImsGeoJsonFeature.Type.FEATURE)
            .withProperties(properties)
            .withGeometry(geometry);

    }

    private static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.TimeAndDuration createTimeAndDuration(final ZonedDateTime releaseTime) {
        return new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.TimeAndDuration(
            releaseTime, releaseTime.plusHours(2),
            new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.EstimatedDuration().withInformal("Yli 6 tuntia")
                .withMaximum(Datex2JsonConverterServiceTest.MAX_DURATION)
                .withMinimum(Datex2JsonConverterServiceTest.MIN_DURATION));
    }

    private static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.LocationDetails createLocationDetails(final boolean areaLocation) {
        return new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.LocationDetails()
            .withAreaLocation(areaLocation ? new AreaLocation().withAreas(Collections.singletonList(new Area("Helsinki", 73, Area.Type.MUNICIPALITY))) : null)
            .withRoadAddressLocation(areaLocation ? null :
                new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.RoadAddressLocation(
                    createRoadPoint(1), createRoadPoint(2), fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.RoadAddressLocation.Direction.POS, "Marjamäen suuntaan")
            );
    }

    private static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.RoadPoint createRoadPoint(final int id) {
        return new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.RoadPoint(
            "Lempäälä" + id, "Pirkanmaa", "Suomi",
            new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.RoadAddress(130, 24, 4000),
            "Tie 123", new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.AlertCLocation(37128, "Marjamäki", 2000));
    }

    private static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.Location createLocation() {
        return new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_12.Location(358, 10, "1.1.1", "Location description");
    }

    private static RoadWorkPhase createRoadWorkPhase(final ZonedDateTime releaseTime, Restriction.Type restrictionType, final String featureName, final Double featureQuantity,
                                                     final String featureUnit, final Worktype.Type wokType) {
        return new RoadWorkPhase()
            .withComment("Työn aloitus")
            .withRestrictions(Collections.singletonList(new Restriction(restrictionType, new Feature(featureName, featureQuantity, featureUnit))))
            .withWorktypes(Collections.singletonList(new Worktype(wokType, wokType.toString())))
            .withId(WORK_PHASE_ID)
            .withLocation(createLocation())
            .withLocationDetails(createLocationDetails(false))
            // V0.2.8.
            .withSeverity(RoadWorkPhase.Severity.HIGH)
            .withTimeAndDuration(createTimeAndDuration(releaseTime))
            .withWorkingHours(Arrays.asList(new WorkingHour(WorkingHour.Weekday.MONDAY, "8:00", "15:30"), new WorkingHour(WorkingHour.Weekday.TUESDAY, "8:00", "15:30")));
    }

}
