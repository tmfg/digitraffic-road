package fi.livi.digitraffic.tie.data.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
    public HistoryRootDataObjectDto listHistoryData(final long linkId, final int year, final int month) {
        if (linkFreeFlowSpeedRepository.linkExists(linkId) != 1) {
            throw new ObjectNotFoundException("Link", linkId);
        }
        final ZonedDateTime updated = DateHelper.toZonedDateTime(dayDataRepository.getLatestMeasurementTime());
        final List<LinkMeasurementDataDto> linkData = dayDataRepository.getAllMedianTravelTimesForLink(linkId, year, month);
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
            linkData.setMeasuredTime(DateHelper.getNewest(linkData.getMeasuredTime(), ld.getMeasuredTime()));
            linkData.getLinkMeasurements().add(ld);
        }
        final List<LinkDataDto> linkDatas = new ArrayList<>(linkDataMap.values());
        linkDatas.sort(Comparator.comparingInt(LinkDataDto::getLinkNumber));
        return linkDatas;
    }
}
