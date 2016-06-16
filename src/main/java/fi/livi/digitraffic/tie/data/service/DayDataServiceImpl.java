package fi.livi.digitraffic.tie.data.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.DayDataRepository;
import fi.livi.digitraffic.tie.data.dto.daydata.HistoryRootDataObjectDto;
import fi.livi.digitraffic.tie.data.dto.daydata.LinkDataDto;
import fi.livi.digitraffic.tie.data.dto.daydata.LinkMeasurementDataDto;
import fi.livi.digitraffic.tie.helper.DateHelpper;

@Service
public class DayDataServiceImpl implements DayDataService {
    private final DayDataRepository dayDataRepository;

    @Autowired
    public DayDataServiceImpl(DayDataRepository dayDataRepository) {
        this.dayDataRepository = dayDataRepository;
    }

    // TODO onlyUpdateInfo: do direct query to get update info
    @Transactional(readOnly = true)
    @Override
    public HistoryRootDataObjectDto listPreviousDayHistoryData(boolean onlyUpdateInfo) {
        final List<LinkMeasurementDataDto> linkData = dayDataRepository.listAllMedianTravelTimes();

        List<LinkDataDto> linkDataTo = new ArrayList<>();
        LocalDateTime updated = convertToDayDataData(linkData, linkDataTo);
        return new HistoryRootDataObjectDto(
                onlyUpdateInfo ? null : linkDataTo,
                updated);
    }

    private static LocalDateTime convertToDayDataData(final List<LinkMeasurementDataDto> linkDataFrom, List<LinkDataDto> linkDataTo) {

        LocalDateTime uptaded = null;
        // LinkDataDto is sorted by linkId, so this works
        LocalDateTime linkUpdated = null;
        LinkDataDto previous = null;
        for(final LinkMeasurementDataDto ld : linkDataFrom) {
            if(previous == null || previous.getLinkNumber() != ld.getLinkId()) {
                linkUpdated = null;
                previous = new LinkDataDto(ld.getLinkId(), new ArrayList<>());
                linkDataTo.add(previous);
            }
            uptaded = DateHelpper.getNewest(uptaded, ld.getMeasured());
            previous.setMeasured(DateHelpper.getNewest(previous.getMeasured(), ld.getMeasured()));
            previous.getLinkMeasurements().add(ld);
        }

        return uptaded;
    }
}
