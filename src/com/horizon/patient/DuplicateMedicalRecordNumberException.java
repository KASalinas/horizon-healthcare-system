package com.horizon.patient;

public class DuplicateMedicalRecordNumberException extends RuntimeException {
    public DuplicateMedicalRecordNumberException(String medicalRecordNumber) {
        super("A patient with MRN " + medicalRecordNumber + " already exists");
    }
}
