package fi.livi.digitraffic.tie.dto.tms;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(type = "string",
        description =
                "DatexII 3.7 XML response. Root element: `d2:payload` with `xsi:type=\"roa:MeasurementSiteTablePublication\"`. " +
                        "[View schema documentation](https://docs.datex2.eu/downloads/modelv37/)")
public class MeasurementSiteTablePublication37Model {
}
