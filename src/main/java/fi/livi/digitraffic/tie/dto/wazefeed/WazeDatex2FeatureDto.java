package fi.livi.digitraffic.tie.dto.wazefeed;

import fi.livi.digitraffic.tie.datex2.v3_7.SituationPublication;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.SituationType;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature;

public class WazeDatex2FeatureDto {
    public final String situationId;
    public final SituationType situationType;
    public final String datex2Message;
    public final SituationPublication situationPublication;
    public final TrafficAnnouncementFeature feature;

    public WazeDatex2FeatureDto(final String situationId,
                                final SituationType situationType,
                                final String datex2Message,
                                final SituationPublication situationPublication,
                                final TrafficAnnouncementFeature feature) {
        this.situationId = situationId;
        this.situationType = situationType;
        this.datex2Message = datex2Message;
        this.situationPublication = situationPublication;
        this.feature = feature;
    }
}
