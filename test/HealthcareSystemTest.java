import java.time.LocalDate;

public class HealthcareSystemTest {
    public static void main(String[] args) {
        registersAndFindsPatient();
        rejectsDuplicateMedicalRecordNumber();
        recordsEncounter();
        System.out.println("All healthcare system tests passed.");
    }

    private static void registersAndFindsPatient() {
        PatientRepository repository = new PatientRepository();
        Patient patient = samplePatient();
        repository.register(patient);

        check(repository.findByMedicalRecordNumber("MRN-2001").orElseThrow() == patient,
                "Registered patient should be found by MRN");
    }

    private static void rejectsDuplicateMedicalRecordNumber() {
        PatientRepository repository = new PatientRepository();
        repository.register(samplePatient());

        try {
            repository.register(samplePatient());
            throw new AssertionError("Duplicate MRN should be rejected");
        } catch (IllegalArgumentException expected) {
            check(expected.getMessage().contains("already exists"),
                    "Duplicate error should explain the problem");
        }
    }

    private static void recordsEncounter() {
        Patient patient = samplePatient();
        patient.recordEncounter(new Encounter(
                LocalDate.now(), "Urgent care", "Dr. Patel", "Follow-up recommended"));

        check(patient.getEncounters().size() == 1, "Encounter should be recorded");
    }

    private static Patient samplePatient() {
        return new Patient(
                "MRN-2001", "Taylor Morgan", LocalDate.of(1990, 6, 20),
                "taylor.morgan@example.test");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
