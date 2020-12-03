package fi.livi.digitraffic.tie.service.datex2;

import static fi.livi.digitraffic.tie.service.datex2.Datex2JsonConverterServiceTest.WORK_PHASE_ID;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;

import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_9.Feature;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_9.ItineraryLeg;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_9.ItineraryRoadLeg;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_9.LastActiveItinerarySegment;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_9.RoadWorkPhase;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_9.TrafficAnnouncement;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_9.TrafficAnnouncementProperties;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_9.WorkingHour;
import fi.livi.digitraffic.tie.metadata.geojson.Point;

public class ImsJsonMessageFactoryV0_2_9 {

    public static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_9.ImsGeoJsonFeature createJsonMessage(
        final ZonedDateTime releaseTime, final String featureName, final Double featureQuantity, final String featureUnit,
        final TrafficAnnouncementProperties.SituationType situationType, final TrafficAnnouncementProperties.TrafficAnnouncementType trafficAnnouncementType) {

        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_9.TrafficAnnouncementProperties
            properties = new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_9.TrafficAnnouncementProperties()
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
                    .withLocationDetails(createLocationDetails())
                    // V0.2.5
                    .withFeatures(Collections.singletonList(new Feature(featureName, featureQuantity, featureUnit)))
                    .withComment("TEST")
                    .withTimeAndDuration(createTimeAndDuration(releaseTime))
                    .withAdditionalInformation("Liikenne- ja kelitiedot verkossa: http://liikennetilanne.tmfg.fi/")
                    .withSender("Tieliikennekeskus Helsinki")
                    .withRoadWorkPhases(Collections.singletonList(createRoadWorkPhase(releaseTime, featureName, featureQuantity, featureUnit)))
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
            .withContact(new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_9.Contact("123456789", "helsinki@liikennekeskus.fi"));
        return new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_9.ImsGeoJsonFeature()
            .withType(fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_9.ImsGeoJsonFeature.Type.FEATURE)
            .withProperties(properties)
            .withGeometry(new Point(23.774741, 61.502211));
    }

    private static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_9.TimeAndDuration createTimeAndDuration(final ZonedDateTime releaseTime) {
        return new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_9.TimeAndDuration(
            releaseTime, releaseTime.plusHours(2),
            new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_9.EstimatedDuration().withInformal("Yli 6 tuntia")
                .withMaximum(Datex2JsonConverterServiceTest.MAX_DURATION)
                .withMinimum(Datex2JsonConverterServiceTest.MIN_DURATION));
    }

    private static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_9.LocationDetails createLocationDetails() {
        return new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_9.LocationDetails()
            .withRoadAddressLocation(
                new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_9.RoadAddressLocation(
                    createRoadPoint(1), createRoadPoint(2), fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_9.RoadAddressLocation.Direction.POS, "Marjamäen suuntaan")
            );
    }

    private static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_9.RoadPoint createRoadPoint(final int id) {
        return new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_9.RoadPoint(
            "Lempäälä" + id, "Pirkanmaa", "Suomi",
            new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_9.RoadAddress(130, 24, 4000),
            "Tie 123", new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_9.AlertCLocation(37128, "Marjamäki", 2000));
    }

    private static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_9.Location createLocation() {
        return new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_9.Location(358, 10, "1.1.1", "Location description");
    }

    private static RoadWorkPhase createRoadWorkPhase(final ZonedDateTime releaseTime, final String featureName, final Double featureQuantity,
                                                     final String featureUnit) {
        return new RoadWorkPhase()
            .withComment("Työn aloitus")
            .withFeatures(Collections.singletonList(new Feature(featureName, featureQuantity, featureUnit)))
            .withId(WORK_PHASE_ID)
            .withLocation(createLocation())
            .withLocationDetails(createLocationDetails())
            // V0.2.8.
            .withSeverity(RoadWorkPhase.Severity.HIGH)
            .withTimeAndDuration(createTimeAndDuration(releaseTime))
            .withWorkingHours(Arrays.asList(new WorkingHour(WorkingHour.Weekday.MA, "8:00", "15:30"), new WorkingHour(WorkingHour.Weekday.TI, "8:00", "15:30")));
    }

}
