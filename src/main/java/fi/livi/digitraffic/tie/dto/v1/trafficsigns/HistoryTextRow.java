package fi.livi.digitraffic.tie.dto.v1.trafficsigns;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface HistoryTextRow {
    int getScreen();
    int getRowNumber();
    String getText();
}
