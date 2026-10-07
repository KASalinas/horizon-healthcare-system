package com.horizon.patient;

public class EncounterNotFoundException extends RuntimeException {
    public EncounterNotFoundException(String medicalRecordNumber, long encounterId) {
        super("No encounter " + encounterId + " found for MRN " + medicalRecordNumber);
    }
}
