package com.horizon.patient;

public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException(String medicalRecordNumber) {
        super("No patient found with MRN " + medicalRecordNumber);
    }
}
