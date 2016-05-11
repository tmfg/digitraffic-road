package fi.livi.digitraffic.tie.data.service;

import fi.livi.digitraffic.tie.data.dto.daydata.HistoryDataObjectDto;

public interface DayDataService {
    HistoryDataObjectDto listPreviousDayHistoryData();
}
