package fi.livi.digitraffic.tie.dto.variablesigns.v1;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Variable Sign text row")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignTextRowV1 {
    @Schema(description = "Screen number")
    public final int screen;

    @Schema(description = "Row number")
    public final int rowNumber;

    @Schema(description = "Text on a row")
    public final String text;

    public SignTextRowV1(final int screen, final int rowNumber, final String text) {
        this.screen = screen;
        this.rowNumber = rowNumber;
        this.text = text;
    }
}
