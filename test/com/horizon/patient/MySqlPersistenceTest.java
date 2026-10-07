package com.horizon.patient;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class MySqlPersistenceTest {
    @Container
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private PatientRepository patientRepository;

    @Test
    void flywaySchemaPersistsPatientAndEncounterInMySql() {
        Patient patient = new Patient("MYSQL-1001", "MySQL Patient",
                LocalDate.of(1990, 1, 1), "mysql@example.test");
        patient.addEncounter(new Encounter(LocalDate.of(2025, 1, 1),
                "Checkup", "Dr. Chen", "Healthy"));
        patientRepository.saveAndFlush(patient);

        Patient reloaded = patientRepository.findByMedicalRecordNumber("MYSQL-1001").orElseThrow();
        assertThat(reloaded.getCreatedAt()).isNotNull();
        assertThat(reloaded.getEncounters()).hasSize(1);
    }
}
