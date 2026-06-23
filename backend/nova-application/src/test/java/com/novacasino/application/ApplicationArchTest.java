package com.novacasino.application;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Hexagonal guard (HU "B" refactor): the application layer must depend only on the domain, common and
 * its own ports — never on infrastructure, the web layer or the Spring framework. Adapters live in
 * {@code nova-infrastructure}; the web layer wires use cases as beans.
 */
class ApplicationArchTest {

    private final JavaClasses applicationClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.novacasino.application");

    @Test
    void application_doesNotDependOnInfrastructureOrWebOrSpring() {
        final ArchRule rule = noClasses()
                .should().dependOnClassesThat().resideInAnyPackage(
                        "com.novacasino.infrastructure..",
                        "com.novacasino.api..",
                        "org.springframework..")
                .as("the application layer must not depend on infrastructure, the web layer or Spring");
        rule.check(applicationClasses);
    }
}
