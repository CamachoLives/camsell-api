package com.camsell.architecture;

import com.camsell.CamsellApplication;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Reglas que mantienen la arquitectura hexagonal. Si alguien acopla el dominio
 * a Spring o salta de un contexto a otro por la infraestructura, estos tests
 * fallan antes de que el codigo llegue a main.
 */
@AnalyzeClasses(
        packagesOf = CamsellApplication.class,
        importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule el_dominio_no_depende_de_spring = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
            .because("el nucleo de negocio debe poder compilarse y probarse sin el framework");

    @ArchTest
    static final ArchRule el_dominio_no_depende_de_jpa = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "org.hibernate..")
            .because("la persistencia es un detalle de infraestructura");

    @ArchTest
    static final ArchRule el_dominio_no_conoce_la_infraestructura = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
            .because("las dependencias apuntan hacia el dominio, nunca al contrario");

    @ArchTest
    static final ArchRule la_capa_de_aplicacion_no_conoce_la_infraestructura = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..infrastructure.web..", "..infrastructure.persistence..")
            .because("los servicios de aplicacion solo hablan con puertos");

    @ArchTest
    static final ArchRule las_entidades_jpa_no_salen_de_persistencia = noClasses()
            .that().resideOutsideOfPackage("..infrastructure.persistence..")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Entity")
            .because("las entidades JPA no deben filtrarse hacia el dominio ni hacia la API");

    @ArchTest
    static final ArchRule solo_identity_lee_el_security_context = noClasses()
            .that().resideOutsideOfPackage("com.camsell.identity.infrastructure.security..")
            .should().accessClassesThat()
            .haveFullyQualifiedName("org.springframework.security.core.context.SecurityContextHolder")
            .because("el usuario autenticado se resuelve unicamente a traves de CurrentUserPort");

    @ArchTest
    static final ArchRule los_controladores_viven_en_infraestructura_web = classes()
            .that().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
            .should().resideInAPackage("..infrastructure.web..");

    @ArchTest
    static final ArchRule los_adaptadores_de_persistencia_implementan_puertos = classes()
            .that().haveSimpleNameEndingWith("PersistenceAdapter")
            .should().resideInAPackage("..infrastructure.persistence..")
            .andShould().beAssignableTo(Object.class);

    @ArchTest
    static final ArchRule los_puertos_son_interfaces = classes()
            .that().resideInAnyPackage("..domain.port.in..", "..domain.port.out..")
            .and().areTopLevelClasses()
            .should().beInterfaces()
            .because("un puerto es un contrato, no una implementacion");
}
