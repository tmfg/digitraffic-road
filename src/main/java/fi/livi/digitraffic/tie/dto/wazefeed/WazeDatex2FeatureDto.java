package fi.livi.digitraffic.tie.dto.wazefeed;

import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2;

public class WazeDatex2FeatureDto {
    public final Datex2 datex2;
    public final D2LogicalModel d2LogicalModel;
    public final TrafficAnnouncementFeature feature;

    public WazeDatex2FeatureDto(final Datex2 datex2, final D2LogicalModel d2LogicalModel, final TrafficAnnouncementFeature feature) {
        this.datex2 = datex2;
        this.d2LogicalModel = d2LogicalModel;
        this.feature = feature;
    }
}
