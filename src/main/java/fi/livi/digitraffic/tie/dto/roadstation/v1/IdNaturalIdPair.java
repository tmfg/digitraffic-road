package fi.livi.digitraffic.tie.dto.roadstation.v1;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface IdNaturalIdPair {
    Long getId();

    Long getNaturalId();

    static Map<Long, Long> getAsIdToNaturalIdMapLongs(final List<IdNaturalIdPair> data) {
        return data.stream()
                .collect(Collectors.toMap(IdNaturalIdPair::getId, IdNaturalIdPair::getNaturalId));
    }
}
