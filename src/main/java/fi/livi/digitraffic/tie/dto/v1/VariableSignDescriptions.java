package fi.livi.digitraffic.tie.dto.v1;

import java.time.Instant;
import java.util.List;

import fi.livi.digitraffic.tie.dto.data.v1.DataUpdatedSupportV1;

public class VariableSignDescriptions implements DataUpdatedSupportV1 {
    public final List<CodeDescription> signTypes;
    private final Instant dataUpdatedTime;

    public VariableSignDescriptions(final List<CodeDescription> signTypes, final Instant dataUpdatedTime) {
        this.signTypes = signTypes;
        this.dataUpdatedTime = dataUpdatedTime;
    }

    @Override
    public Instant getDataUpdatedTime() {
        return dataUpdatedTime;
    }
}
