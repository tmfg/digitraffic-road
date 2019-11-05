package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.dto.CodeDescriptionJson;

@Repository
public interface CodeDescriptionRepository extends SqlRepository {
    @Query(value = "select code, description from code_description where domain = 'VARIABLE_SIGN'", nativeQuery = true)
    List<CodeDescriptionJson> listAllVariableSignTypes();
}
