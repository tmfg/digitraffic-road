package fi.livi.digitraffic.tie.metadata.dto.location;

import org.springframework.beans.factory.annotation.Value;

public interface LocationSubtypeJson {
    @Value("#{target.id.subtypeCode}")
    String getSubtypeCode();

    String getDescriptionEn();

    String getDescriptionFi();
}
