package fi.livi.digitraffic.tie.metadata.dto.location;

import org.springframework.beans.factory.annotation.Value;

public interface LocationTypeJson {
    @Value("#{target.id.typeCode}")
    String getTypeCode();

    String getDescriptionEn();

    String getDescriptionFi();
}
