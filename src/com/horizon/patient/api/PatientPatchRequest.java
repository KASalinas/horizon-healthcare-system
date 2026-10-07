package com.horizon.patient.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PatientPatchRequest(
        @Size(min = 1, max = 200) String fullName,
        @PastOrPresent LocalDate dateOfBirth,
        @Email @Size(min = 1, max = 320) String email) {
}
