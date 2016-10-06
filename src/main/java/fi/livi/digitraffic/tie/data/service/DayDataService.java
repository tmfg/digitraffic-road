package fi.livi.digitraffic.tie.data.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.DayDataRepository;
import fi.livi.digitraffic.tie.data.dao.LinkFreeFlowSpeedRepository;
import fi.livi.digitraffic.tie.data.dto.daydata.HistoryRootDataObjectDto;
import fi.livi.digitraffic.tie.data.dto.daydata.LinkDataDto;
import fi.livi.digitraffic.tie.data.dto.daydata.LinkMeasurementDataDto;
import fi.livi.digitraffic.tie.helper.DateHelper;

@Service
public class DayDataService {
    private final DayDataRepository dayDataRepository;
    private final LinkFreeFlowSpeedRepository linkFreeFlowSpeedRepository;

    @Autowired
    public DayDataService(final DayDataRepository dayDataRepository,
                          final LinkFreeFlowSpeedRepository linkFreeFlowSpeedRepository) {
        this.dayDataRepository = dayDataRepository;
        this.linkFreeFlowSpeedRepository = linkFreeFlowSpeedRepository;
    }

    @Transactional(readOnly = true)
    public HistoryRootDataObjectDto listPreviousDayHistoryData(final boolean onlyUpdateInfo) {

        LocalDateTime updated = dayDataRepository.getLatestMeasurementTime();

        if (onlyUpdateInfo) {
            // If there is no data for previous day, return update time before previousday
            if (updated == null) {
                updated = LocalDateTime.now().minusDays(2).with(LocalTime.MAX);
            }
            return new HistoryRootDataObjectDto(updated);

        } else {

            final List<LinkMeasurementDataDto> linkData = dayDataRepository.listAllMedianTravelTimes();

            return new HistoryRootDataObjectDto(
                    convertToDayDataData(linkData),
                    updated);
        }
    }

    @Transactional(readOnly = true)
    public HistoryRootDataObjectDto listPreviousDayHistoryData(long linkId) {
        if (1 != linkFreeFlowSpeedRepository.linkExists(linkId)) {
            throw new ObjectNotFoundException("Link", linkId);
        }
        LocalDateTime updated = dayDataRepository.getLatestMeasurementTime();
        List<LinkMeasurementDataDto> linkData = dayDataRepository.getAllMedianTravelTimesForLink(linkId);
        return new HistoryRootDataObjectDto(
                convertToDayDataData(linkData),
                updated);
    }

    private static List<LinkDataDto> convertToDayDataData(final List<LinkMeasurementDataDto> linkDataFrom) {
        final HashMap<Integer, LinkDataDto> linkDataMap = new HashMap<>();
        for(final LinkMeasurementDataDto ld : linkDataFrom) {
            LinkDataDto linkData = linkDataMap.get(ld.getLinkId());
            if(linkData == null) {
                linkData = new LinkDataDto(ld.getLinkId(), new ArrayList<>());
                linkDataMap.put(ld.getLinkId(), linkData);
            }
            linkData.setMeasured(DateHelper.getNewest(linkData.getMeasured(), ld.getMeasured()));
            linkData.getLinkMeasurements().add(ld);
        }
        final List<LinkDataDto> linkDatas = new ArrayList<>(linkDataMap.values());
        linkDatas.sort((o1, o2) -> Integer.compare(o1.getLinkNumber(), o2.getLinkNumber()));
        return linkDatas;
    }
}
