package fi.livi.digitraffic.tie.data.service;

import fi.livi.digitraffic.tie.data.model.daydata.HistoryDataObject;

public interface DayDataService {
    HistoryDataObject listPreviousDayHistoryData();
}
