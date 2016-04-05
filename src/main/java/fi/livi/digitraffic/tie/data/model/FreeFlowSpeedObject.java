package fi.livi.digitraffic.tie.data.model;

import java.util.List;

public class FreeFlowSpeedObject extends DataObject {
    private final List<LinkFreeFlowSpeed> linkData;
    private final List<LamFreeFlowSpeed> lamData;

    public FreeFlowSpeedObject(final List<LinkFreeFlowSpeed> linkData, final List<LamFreeFlowSpeed> lamData) {
        this.linkData = linkData;
        this.lamData = lamData;
    }

    public List<LinkFreeFlowSpeed> getLinkData() {
        return linkData;
    }

    public List<LamFreeFlowSpeed> getLamData() {
        return lamData;
    }
}
