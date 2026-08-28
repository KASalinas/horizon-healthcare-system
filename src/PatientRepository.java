import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class PatientRepository {
    private final Map<String, Patient> patientsByMrn = new LinkedHashMap<>();

    public void register(Patient patient) {
        Objects.requireNonNull(patient, "Patient is required");
        String mrn = patient.getMedicalRecordNumber();
        if (patientsByMrn.containsKey(mrn)) {
            throw new IllegalArgumentException("A patient with MRN " + mrn + " already exists");
        }
        patientsByMrn.put(mrn, patient);
    }

    public Optional<Patient> findByMedicalRecordNumber(String medicalRecordNumber) {
        if (medicalRecordNumber == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(patientsByMrn.get(medicalRecordNumber.trim()));
    }

    public List<Patient> findAll() {
        return List.copyOf(patientsByMrn.values());
    }
}
