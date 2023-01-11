package fi.livi.digitraffic.tie.dao.variablesign.v1;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dao.SqlRepository;
import fi.livi.digitraffic.tie.dto.v1.CodeDescription;

@Repository
public interface CodeDescriptionRepositoryV1 extends SqlRepository {
    @Query(value =
        "select code, description_fi description, description_en descriptionEn " +
        "from code_description where domain = 'VARIABLE_SIGN'",
        nativeQuery = true)
    List<CodeDescription> listAllVariableSignTypes();

    @Query(value =
           "select max(modified)\n" +
           "from code_description", nativeQuery = true)
    Instant getLastUpdated();
}
