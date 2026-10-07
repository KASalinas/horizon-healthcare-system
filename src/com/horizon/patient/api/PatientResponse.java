package com.horizon.patient.api;

import com.horizon.patient.Patient;

import java.time.LocalDate;

public record PatientResponse(
        Long id,
        String medicalRecordNumber,
        String fullName,
        LocalDate dateOfBirth,
        String email) {

    public static PatientResponse from(Patient patient) {
        return new PatientResponse(patient.getId(), patient.getMedicalRecordNumber(),
                patient.getFullName(), patient.getDateOfBirth(), patient.getEmail());
    }
}
