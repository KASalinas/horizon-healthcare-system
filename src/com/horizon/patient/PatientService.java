package com.horizon.patient;

import com.horizon.patient.api.EncounterRequest;
import com.horizon.patient.api.EncounterResponse;
import com.horizon.patient.api.PatientRequest;
import com.horizon.patient.api.PatientResponse;
import com.horizon.patient.api.PatientUpdateRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PatientService {
    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional
    public PatientResponse register(PatientRequest request) {
        String mrn = request.medicalRecordNumber().trim();
        if (patientRepository.existsByMedicalRecordNumber(mrn)) {
            throw new DuplicateMedicalRecordNumberException(mrn);
        }

        Patient patient = new Patient(mrn, request.fullName().trim(),
                request.dateOfBirth(), request.email().trim());
        try {
            return PatientResponse.from(patientRepository.saveAndFlush(patient));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateMedicalRecordNumberException(mrn);
        }
    }

    public PatientResponse find(String medicalRecordNumber) {
        return PatientResponse.from(findEntity(medicalRecordNumber));
    }

    public List<PatientResponse> findAll() {
        return patientRepository.findAll(Sort.by("id")).stream()
                .map(PatientResponse::from)
                .toList();
    }

    @Transactional
    public PatientResponse update(String medicalRecordNumber, PatientUpdateRequest request) {
        Patient patient = findEntity(medicalRecordNumber);
        patient.update(request.fullName().trim(), request.dateOfBirth(), request.email().trim());
        return PatientResponse.from(patient);
    }

    @Transactional
    public EncounterResponse addEncounter(String medicalRecordNumber, EncounterRequest request) {
        Patient patient = findEntity(medicalRecordNumber);
        Encounter encounter = new Encounter(request.date(), request.type().trim(),
                request.clinician().trim(), request.notes() == null ? "" : request.notes().trim());
        patient.addEncounter(encounter);
        patientRepository.flush();
        return EncounterResponse.from(encounter);
    }

    public List<EncounterResponse> findEncounters(String medicalRecordNumber) {
        return findEntity(medicalRecordNumber).getEncounters().stream()
                .map(EncounterResponse::from)
                .toList();
    }

    private Patient findEntity(String medicalRecordNumber) {
        String mrn = medicalRecordNumber.trim();
        return patientRepository.findByMedicalRecordNumber(mrn)
                .orElseThrow(() -> new PatientNotFoundException(mrn));
    }
}
