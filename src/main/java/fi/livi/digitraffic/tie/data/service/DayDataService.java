package fi.livi.digitraffic.tie.data.service;

import fi.livi.digitraffic.tie.data.dto.daydata.HistoryRootDataObjectDto;

public interface DayDataService {
    HistoryRootDataObjectDto listPreviousDayHistoryData();
}
