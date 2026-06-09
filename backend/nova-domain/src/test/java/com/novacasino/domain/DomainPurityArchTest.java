package com.novacasino.domain;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * HU-1-QA-01 · AC3 — {@code nova-domain} must stay framework-free: no class may depend on Spring or
 * JPA. This guards the "data-oriented core + DDD shell" invariant (readme §2.1.7): the engine and the
 * domain are pure Java so they can be reused by the simulator without a Spring context. If anyone
 * couples Spring/JPA into the domain, this test fails the build.
 */
class DomainPurityArchTest {

    private final JavaClasses domainClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.novacasino.domain");

    @Test
    void domainDoesNotDependOnSpring() {
        noClasses()
                .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                .because("nova-domain must remain free of Spring (readme §2.1.7)")
                .check(domainClasses);
    }

    @Test
    void domainDoesNotDependOnJpa() {
        noClasses()
                .should().dependOnClassesThat().resideInAPackage("jakarta.persistence..")
                .because("nova-domain must remain free of JPA (readme §2.1.7)")
                .check(domainClasses);
    }
}
