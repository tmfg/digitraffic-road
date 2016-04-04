package fi.livi.digitraffic.tie.data.model;

import java.time.ZonedDateTime;
import java.util.List;

public class LamDataObject extends DataObject {
    private final List<LamMeasurement> dynamicLamData;

    public LamDataObject(final List<LamMeasurement> dynamicLamData) {
        super(ZonedDateTime.now());
        this.dynamicLamData = dynamicLamData;
    }

    public List<LamMeasurement> getDynamicLamData() {
        return dynamicLamData;
    }
}
