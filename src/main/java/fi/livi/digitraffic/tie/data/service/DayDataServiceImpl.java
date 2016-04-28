package fi.livi.digitraffic.tie.data.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.data.dao.DayDataRepository;
import fi.livi.digitraffic.tie.data.model.daydata.HistoryDataObject;
import fi.livi.digitraffic.tie.data.model.daydata.LinkData;
import fi.livi.digitraffic.tie.data.model.daydata.LinkDynamicData;

@Service
public class DayDataServiceImpl implements DayDataService {
    private final DayDataRepository dayDataRepository;

    @Autowired
    public DayDataServiceImpl(DayDataRepository dayDataRepository) {
        this.dayDataRepository = dayDataRepository;
    }

    @Override
    public HistoryDataObject listPreviousDayHistoryData() {
        final List<LinkData> linkData = dayDataRepository.listAllMedianTravelTimes();

        return new HistoryDataObject(convertToDynamicData(linkData));
    }

    private static List<LinkDynamicData> convertToDynamicData(final List<LinkData> linkData) {
        final List<LinkDynamicData> list = new ArrayList<>();

        // LinkData is sorted by linkId, so this works
        LinkDynamicData previous = null;
        for(final LinkData ld : linkData) {
            if(previous == null || previous.getLinkNumber() != ld.getLinkId()) {
                previous = new LinkDynamicData(ld.getLinkId(), new ArrayList<>());
                list.add(previous);
            }

            previous.getLinkData().add(ld);
        }

        return list;
    }
}
