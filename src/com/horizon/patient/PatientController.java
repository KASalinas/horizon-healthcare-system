package com.horizon.patient;

import com.horizon.patient.api.EncounterRequest;
import com.horizon.patient.api.EncounterResponse;
import com.horizon.patient.api.PatientRequest;
import com.horizon.patient.api.PatientResponse;
import com.horizon.patient.api.PatientUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientResponse register(@Valid @RequestBody PatientRequest request) {
        return patientService.register(request);
    }

    @GetMapping("/{medicalRecordNumber}")
    public PatientResponse find(@PathVariable String medicalRecordNumber) {
        return patientService.find(medicalRecordNumber);
    }

    @GetMapping
    public List<PatientResponse> list() {
        return patientService.findAll();
    }

    @PutMapping("/{medicalRecordNumber}")
    public PatientResponse update(@PathVariable String medicalRecordNumber,
                                  @Valid @RequestBody PatientUpdateRequest request) {
        return patientService.update(medicalRecordNumber, request);
    }

    @PostMapping("/{medicalRecordNumber}/encounters")
    @ResponseStatus(HttpStatus.CREATED)
    public EncounterResponse addEncounter(@PathVariable String medicalRecordNumber,
                                          @Valid @RequestBody EncounterRequest request) {
        return patientService.addEncounter(medicalRecordNumber, request);
    }

    @GetMapping("/{medicalRecordNumber}/encounters")
    public List<EncounterResponse> listEncounters(@PathVariable String medicalRecordNumber) {
        return patientService.findEncounters(medicalRecordNumber);
    }
}
