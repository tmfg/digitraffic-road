package fi.livi.digitraffic.tie.dto.wazefeed;

import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2;

public class WazeDatex2FeatureDto {
    public final Datex2 datex2;
    public final TrafficAnnouncementFeature feature;

    public WazeDatex2FeatureDto(final Datex2 datex2, final TrafficAnnouncementFeature feature) {
        this.datex2 = datex2;
        this.feature = feature;
    }
}
