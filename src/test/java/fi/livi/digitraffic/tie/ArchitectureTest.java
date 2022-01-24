package fi.livi.digitraffic.tie;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

import fi.livi.digitraffic.tie.annotation.NotTransactionalServiceMethod;

/**
 * https://www.archunit.org/userguide/html/000_Index.html
 */
public class ArchitectureTest extends AbstractTest {

    private final JavaClasses importedClasses = new ClassFileImporter()
        .withImportOption(location -> !location.contains("Test.") && !location.contains("test-classes"))
        .importPackages("fi.livi.digitraffic");

    @Test
    public void publicServiceMethodMustBeTransactional() {
        ArchRuleDefinition
            .methods()
            .that()
            .arePublic()
            .and()
            .areNotStatic()
            .and()
            .areDeclaredInClassesThat()
            .areAnnotatedWith(Service.class)
            .and()
            // Skip test classes
            .areDeclaredInClassesThat()
            .haveSimpleNameNotContaining("Test")
            .and()
            .areNotAnnotatedWith(NotTransactionalServiceMethod.class)
            .should()
            .beAnnotatedWith(Transactional.class)
            .check(importedClasses);
    }

    @Test
    public void classesAnnotatedWithComponentShouldNotHaveServiceInName() {
        ArchRuleDefinition
            .classes()
            .that()
            .areAnnotatedWith(Component.class)
            .should()
            .haveSimpleNameNotEndingWith("Service")
            .check(importedClasses);
    }

    @Test
    public void classesAnnotatedWithConfigurationNameShouldEndWithConfiguration() {
        ArchRuleDefinition
            .classes()
            .that()
            .areAnnotatedWith(Configuration.class)
            .should()
            .haveSimpleNameEndingWith("Configuration")
            .check(importedClasses);
    }
}
