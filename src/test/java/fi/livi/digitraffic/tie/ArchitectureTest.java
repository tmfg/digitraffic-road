package fi.livi.digitraffic.tie;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

import fi.livi.digitraffic.tie.annotation.NotTransactionalServiceMethod;

/**
 * <a href="https://www.archunit.org/userguide/html/000_Index.html">ArchUnit User Guide</a>
 */
public class ArchitectureTest extends AbstractTest {
    private static final Logger log = LoggerFactory.getLogger(ArchitectureTest.class);

    private final JavaClasses importedClasses;

    public ArchitectureTest() {
        final StopWatch timer = StopWatch.createStarted();
        importedClasses = new ClassFileImporter()
            .withImportOption(location ->
                location.contains("/target/classes/") &&
                location.contains("fi/livi/digitraffic") &&
                // Skip generated classes
                !location.contains("fi/livi/digitraffic/tie/datex2") &&
                !location.contains("fi/livi/digitraffic/tie/external"))
            .importPath("target/classes/fi/livi/digitraffic/tie/");

        log.info("Reading classes took {} s", timer.getTime()/1000.0);
    }

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
            .haveSimpleNameNotContaining("Service")
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
