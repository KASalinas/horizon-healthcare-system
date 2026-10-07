package com.horizon.patient.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PatientRequest(
        @NotBlank @Size(max = 64) String medicalRecordNumber,
        @NotBlank @Size(max = 200) String fullName,
        @NotNull @PastOrPresent LocalDate dateOfBirth,
        @NotBlank @Email @Size(max = 320) String email) {
}
