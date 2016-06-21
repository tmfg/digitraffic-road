package fi.livi.digitraffic.tie.data.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.DayDataRepository;
import fi.livi.digitraffic.tie.data.dto.daydata.HistoryRootDataObjectDto;
import fi.livi.digitraffic.tie.data.dto.daydata.LinkDataDto;
import fi.livi.digitraffic.tie.data.dto.daydata.LinkMeasurementDataDto;
import fi.livi.digitraffic.tie.helper.DateHelper;

@Service
public class DayDataServiceImpl implements DayDataService {
    private final DayDataRepository dayDataRepository;

    @Autowired
    public DayDataServiceImpl(DayDataRepository dayDataRepository) {
        this.dayDataRepository = dayDataRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public HistoryRootDataObjectDto listPreviousDayHistoryData(boolean onlyUpdateInfo) {

        LocalDateTime updated = dayDataRepository.getLatestMeasurementTime();

        if (onlyUpdateInfo) {
            // If there is no data for previous day, return update time before previousday
            if (updated == null) {
                updated = LocalDateTime.now().minusDays(2).with(LocalTime.MAX);
            }
            return new HistoryRootDataObjectDto(updated);

        } else {

            final List<LinkMeasurementDataDto> linkData = dayDataRepository.listAllMedianTravelTimes();

            return new HistoryRootDataObjectDto(convertToDayDataData(linkData),
                    updated);
        }
    }

    private static List<LinkDataDto> convertToDayDataData(final List<LinkMeasurementDataDto> linkDataFrom) {

        List<LinkDataDto> linkDataTo = new ArrayList<>();
        // LinkDataDto is sorted by linkId, so this works
        LocalDateTime linkUpdated = null;
        LinkDataDto previous = null;
        for(final LinkMeasurementDataDto ld : linkDataFrom) {
            if(previous == null || previous.getLinkNumber() != ld.getLinkId()) {
                linkUpdated = null;
                previous = new LinkDataDto(ld.getLinkId(), new ArrayList<>());
                linkDataTo.add(previous);
            }
            previous.setMeasured(DateHelper.getNewest(previous.getMeasured(), ld.getMeasured()));
            previous.getLinkMeasurements().add(ld);
        }
        return linkDataTo;
    }
}
