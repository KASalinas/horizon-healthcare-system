package com.horizon.patient.api;

import com.horizon.patient.Patient;

import java.time.LocalDate;
import java.time.Instant;

public record PatientResponse(
        Long id,
        String medicalRecordNumber,
        String fullName,
        LocalDate dateOfBirth,
        String email,
        Instant createdAt,
        Instant updatedAt) {

    public static PatientResponse from(Patient patient) {
        return new PatientResponse(patient.getId(), patient.getMedicalRecordNumber(),
                patient.getFullName(), patient.getDateOfBirth(), patient.getEmail(),
                patient.getCreatedAt(), patient.getUpdatedAt());
    }
}
