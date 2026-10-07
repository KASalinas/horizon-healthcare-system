package com.horizon.patient;

import com.horizon.patient.api.EncounterRequest;
import com.horizon.patient.api.EncounterResponse;
import com.horizon.patient.api.PatientRequest;
import com.horizon.patient.api.PatientResponse;
import com.horizon.patient.api.PatientPatchRequest;
import com.horizon.patient.api.PatientUpdateRequest;
import com.horizon.patient.api.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@Validated
public class PatientController {
    private static final List<String> ALLOWED_SORT_FIELDS = List.of(
            "id", "medicalRecordNumber", "fullName", "dateOfBirth", "createdAt");
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
    public PageResponse<PatientResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        if (!ALLOWED_SORT_FIELDS.contains(sort)) {
            throw new IllegalArgumentException("Unsupported patient sort field: " + sort);
        }
        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Sort direction must be asc or desc");
        }
        return PageResponse.from(patientService.findAll(
                PageRequest.of(page, size, Sort.by(sortDirection, sort))));
    }

    @PutMapping("/{medicalRecordNumber}")
    public PatientResponse update(@PathVariable String medicalRecordNumber,
                                  @Valid @RequestBody PatientUpdateRequest request) {
        return patientService.update(medicalRecordNumber, request);
    }

    @PatchMapping("/{medicalRecordNumber}")
    public PatientResponse patch(@PathVariable String medicalRecordNumber,
                                 @Valid @RequestBody PatientPatchRequest request) {
        return patientService.patch(medicalRecordNumber, request);
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

    @GetMapping("/{medicalRecordNumber}/encounters/{encounterId}")
    public EncounterResponse findEncounter(@PathVariable String medicalRecordNumber,
                                           @PathVariable long encounterId) {
        return patientService.findEncounter(medicalRecordNumber, encounterId);
    }
}
