package fi.livi.digitraffic.tie.dto.variablesigns.v1;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface HistoryTextRowV1 {
    int getScreen();
    int getRowNumber();
    String getText();
}
