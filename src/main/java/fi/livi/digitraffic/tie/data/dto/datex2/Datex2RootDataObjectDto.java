package fi.livi.digitraffic.tie.data.dto.datex2;

import java.time.ZonedDateTime;
import java.util.List;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import fi.livi.digitraffic.tie.data.model.Datex2;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Immutable
@ApiModel(value = "Datex2Data", description = "Traffic disorders", parent = RootDataObjectDto.class)
@JsonPropertyOrder({ "dataUpdatedLocalTime", "dataUpdatedUtc", "datex2s"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Datex2RootDataObjectDto extends RootDataObjectDto {

    @ApiModelProperty(value = "Traffic disorders data")
    private final List<Datex2> datex2s;

    public Datex2RootDataObjectDto(final List<Datex2> datex2s, final ZonedDateTime updated) {
        super(updated);
        this.datex2s = datex2s;
    }

    public Datex2RootDataObjectDto(final ZonedDateTime updated) {
        this(null, updated);
    }

    public List<Datex2> getDatex2s() {
        return datex2s;
    }

}

