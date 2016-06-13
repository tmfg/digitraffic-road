package fi.livi.digitraffic.tie.data.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.data.dao.DayDataRepository;
import fi.livi.digitraffic.tie.data.dto.daydata.HistoryRootDataObjectDto;
import fi.livi.digitraffic.tie.data.dto.daydata.LinkData;
import fi.livi.digitraffic.tie.data.dto.daydata.LinkMeasurementDataDto;

@Service
public class DayDataServiceImpl implements DayDataService {
    private final DayDataRepository dayDataRepository;

    @Autowired
    public DayDataServiceImpl(DayDataRepository dayDataRepository) {
        this.dayDataRepository = dayDataRepository;
    }

    @Override
    public HistoryRootDataObjectDto listPreviousDayHistoryData() {
        final List<LinkMeasurementDataDto> linkData = dayDataRepository.listAllMedianTravelTimes();

        return new HistoryRootDataObjectDto(convertToDynamicData(linkData));
    }

    private static List<LinkData> convertToDynamicData(final List<LinkMeasurementDataDto> linkData) {
        final List<LinkData> list = new ArrayList<>();

        // LinkData is sorted by linkId, so this works
        LinkData previous = null;
        for(final LinkMeasurementDataDto ld : linkData) {
            if(previous == null || previous.getLinkNumber() != ld.getLinkId()) {
                previous = new LinkData(ld.getLinkId(), new ArrayList<>());
                list.add(previous);
            }

            previous.getLinkMeasurements().add(ld);
        }

        return list;
    }
}
