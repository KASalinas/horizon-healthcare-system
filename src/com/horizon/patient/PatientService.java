package com.horizon.patient;

import com.horizon.patient.api.EncounterRequest;
import com.horizon.patient.api.EncounterResponse;
import com.horizon.patient.api.PatientRequest;
import com.horizon.patient.api.PatientResponse;
import com.horizon.patient.api.PatientPatchRequest;
import com.horizon.patient.api.PatientUpdateRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<PatientResponse> findAll(Pageable pageable) {
        return patientRepository.findAll(pageable).map(PatientResponse::from);
    }

    @Transactional
    public PatientResponse update(String medicalRecordNumber, PatientUpdateRequest request) {
        Patient patient = findEntity(medicalRecordNumber);
        patient.update(request.fullName().trim(), request.dateOfBirth(), request.email().trim());
        return PatientResponse.from(patientRepository.saveAndFlush(patient));
    }

    @Transactional
    public PatientResponse patch(String medicalRecordNumber, PatientPatchRequest request) {
        if (request.fullName() == null && request.dateOfBirth() == null && request.email() == null) {
            throw new IllegalArgumentException("At least one patient field must be provided");
        }

        Patient patient = findEntity(medicalRecordNumber);
        String fullName = request.fullName() == null
                ? patient.getFullName() : request.fullName().trim();
        String email = request.email() == null ? patient.getEmail() : request.email().trim();
        if (fullName.isBlank() || email.isBlank()) {
            throw new IllegalArgumentException("Updated text fields cannot be blank");
        }
        patient.update(fullName,
                request.dateOfBirth() == null ? patient.getDateOfBirth() : request.dateOfBirth(),
                email);
        return PatientResponse.from(patientRepository.saveAndFlush(patient));
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

    public EncounterResponse findEncounter(String medicalRecordNumber, long encounterId) {
        Patient patient = findEntity(medicalRecordNumber);
        return patient.getEncounters().stream()
                .filter(encounter -> encounter.getId().equals(encounterId))
                .findFirst()
                .map(EncounterResponse::from)
                .orElseThrow(() -> new EncounterNotFoundException(
                        patient.getMedicalRecordNumber(), encounterId));
    }

    private Patient findEntity(String medicalRecordNumber) {
        String mrn = medicalRecordNumber.trim();
        return patientRepository.findByMedicalRecordNumber(mrn)
                .orElseThrow(() -> new PatientNotFoundException(mrn));
    }
}
