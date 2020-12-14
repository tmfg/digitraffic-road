package fi.livi.digitraffic.tie.service.datex2;

import java.time.ZonedDateTime;
import java.util.Collections;

import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.AlertCLocation;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.Contact;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.EstimatedDuration;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.ImsGeoJsonFeature;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.Location;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.LocationDetails;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.LocationToDisplay;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.RoadAddress;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.RoadAddressLocation;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.RoadPoint;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.TimeAndDuration;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.TrafficAnnouncement;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.TrafficAnnouncementProperties;
import fi.livi.digitraffic.tie.metadata.geojson.Point;

public class ImsJsonMessageFactoryV0_2_4 {

    public static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.ImsGeoJsonFeature createJsonMessage(
        final ZonedDateTime releaseTime, final String featureName) {

        final TrafficAnnouncementProperties properties = new TrafficAnnouncementProperties()
            .withVersion(1)
            .withSituationId("GUID123456")
            .withReleaseTime(releaseTime)
            .withAnnouncements(Collections.singletonList(
                new TrafficAnnouncement()
                    .withLanguage(TrafficAnnouncement.Language.FI)
                    .withTitle("Title")
                    .withLocation(createLocation())
                    .withLocationDetails(createLocationDetails())
                    .withFeatures(Collections.singletonList(featureName))
                    .withComment("TEST")
                    .withTimeAndDuration(createTimeAndDuration(releaseTime))
                    .withAdditionalInformation("Liikenne- ja kelitiedot verkossa: http://liikennetilanne.tmfg.fi/")
                    .withSender("Tieliikennekeskus Helsinki")
            ))
            .withLocationToDisplay(new LocationToDisplay(1.0, 2.0))
            .withContact(new Contact("123456789", "987654321", "helsinki@liikennekeskus.fi"));
        return new ImsGeoJsonFeature()
            .withType(ImsGeoJsonFeature.Type.FEATURE)
            .withProperties(properties)
            .withGeometry(new Point(23.774741, 61.502211));
    }

    private static TimeAndDuration createTimeAndDuration(final ZonedDateTime releaseTime) {
        return new TimeAndDuration(releaseTime, releaseTime.plusHours(2),
            new EstimatedDuration().withInformal("Yli 6 tuntia")
                .withMaximum(Datex2JsonConverterServiceTest.MAX_DURATION)
                .withMinimum(Datex2JsonConverterServiceTest.MIN_DURATION));
    }

    private static LocationDetails createLocationDetails() {
        return new LocationDetails()
            .withRoadAddressLocation(
                new RoadAddressLocation(
                    createRoadPoint(1), createRoadPoint(2), RoadAddressLocation.Direction.POS, "Marjamäen suuntaan")
            );
    }

    private static RoadPoint createRoadPoint(final int id) {
        return new RoadPoint(
            "Lempäälä" + id, "Pirkanmaa", "Suomi",
            new RoadAddress(130, 24, 4000),
            "Tie 123", new AlertCLocation(37128, "Marjamäki", 2000));
    }

    private static Location createLocation() {
        return new Location(358, 10, "1.1.1", "Location description");
    }


}
