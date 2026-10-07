package com.horizon.patient.api;

import com.horizon.patient.Encounter;

import java.time.LocalDate;

public record EncounterResponse(
        Long id,
        LocalDate date,
        String type,
        String clinician,
        String notes) {

    public static EncounterResponse from(Encounter encounter) {
        return new EncounterResponse(encounter.getId(), encounter.getDate(), encounter.getType(),
                encounter.getClinician(), encounter.getNotes());
    }
}
