package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.dto.CodeDescription;

@Repository
public interface CodeDescriptionRepository extends SqlRepository {
    @Query(value =
        "select code, description_fi description " +
        "from code_description where domain = 'VARIABLE_SIGN'",
        nativeQuery = true)
    List<CodeDescription> listAllVariableSignTypes();
}
