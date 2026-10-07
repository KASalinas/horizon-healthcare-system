package com.horizon.patient;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class PatientPersistenceTest {
    @Autowired
    private PatientRepository patientRepository;

    @Test
    void persistsPatientAndEncounterRelationship() {
        Patient patient = patient("MRN-2001");
        patient.addEncounter(new Encounter(LocalDate.of(2025, 1, 15),
                "Urgent care", "Dr. Patel", "Follow-up recommended"));
        patientRepository.saveAndFlush(patient);

        Patient reloaded = patientRepository.findByMedicalRecordNumber("MRN-2001").orElseThrow();
        assertThat(reloaded.getEncounters()).hasSize(1);
        assertThat(reloaded.getEncounters().getFirst().getClinician()).isEqualTo("Dr. Patel");
    }

    @Test
    void databaseConstraintProtectsMedicalRecordNumberUniqueness() {
        patientRepository.saveAndFlush(patient("MRN-2001"));

        assertThatThrownBy(() -> patientRepository.saveAndFlush(patient("MRN-2001")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Patient patient(String mrn) {
        return new Patient(mrn, "Taylor Morgan", LocalDate.of(1990, 6, 20),
                "taylor@example.test");
    }
}
