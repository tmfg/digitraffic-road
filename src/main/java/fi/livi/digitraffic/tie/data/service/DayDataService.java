package fi.livi.digitraffic.tie.data.service;

import fi.livi.digitraffic.tie.data.model.daydata.HistoryData;

public interface DayDataService {
    HistoryData listPreviousDayHistoryData();
}
