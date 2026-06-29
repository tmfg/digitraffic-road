package fi.livi.digitraffic.tie.dto.trafficmessage.datex2;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(type = "string",
        description =
                "Datex II 3.5 XML response. Root element: `d2:payload` with `xsi:type=\"sit:SituationPublication\"`. " +
                        "[View schema documentation](https://docs.datex2.eu/downloads/modelv35/)")
public class SituationPublication35Model {
}
