package com.horizon.patient.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EncounterRequest(
        @NotNull @PastOrPresent LocalDate date,
        @NotBlank @Size(max = 100) String type,
        @NotBlank @Size(max = 200) String clinician,
        @Size(max = 4000) String notes) {
}
