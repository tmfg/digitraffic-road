package fi.livi.digitraffic.tie.data.model.daydata;

import java.util.List;

public class LinkDynamicData {
    private final int linkNumber;

    private final List<LinkData> linkData;

    public LinkDynamicData(int linkNumber, List<LinkData> linkData) {
        this.linkNumber = linkNumber;
        this.linkData = linkData;
    }

    public int getLinkNumber() {
        return linkNumber;
    }

    public List<LinkData> getLinkData() {
        return linkData;
    }
}
