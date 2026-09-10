package fi.livi.digitraffic.tie.dto.tms;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(type = "string",
        description =
                "DatexII 3.5 XML response. Root element: `d2:payload` with `xsi:type=\"sit:MeasurementSiteTablePublication\"`. " +
                        "[View schema documentation](https://docs.datex2.eu/downloads/modelv35/)")
public class MeasurementSiteTablePublication35Model {
}

