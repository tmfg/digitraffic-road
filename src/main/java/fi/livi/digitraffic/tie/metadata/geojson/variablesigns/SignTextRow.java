package fi.livi.digitraffic.tie.metadata.geojson.variablesigns;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Variable Sign text row")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignTextRow {
    @Schema(description = "Screen number")
    public final int screen;

    @Schema(description = "Row number")
    public final int rowNumber;

    @Schema(description = "Text on a row")
    public final String text;

    public SignTextRow(final int screen, final int rowNumber, final String text) {
        this.screen = screen;
        this.rowNumber = rowNumber;
        this.text = text;
    }
}
