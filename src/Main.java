import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        PatientRepository patients = new PatientRepository();

        Patient patient = new Patient(
                "MRN-1001",
                "Jordan Lee",
                LocalDate.of(1988, 4, 12),
                "jordan.lee@example.test");
        patients.register(patient);

        Patient foundPatient = patients.findByMedicalRecordNumber("MRN-1001")
                .orElseThrow();
        foundPatient.recordEncounter(new Encounter(
                LocalDate.now(),
                "Primary care",
                "Dr. Rivera",
                "Routine wellness visit"));

        System.out.println("Horizon Healthcare Information System");
        System.out.println(foundPatient.summary());
        System.out.println("Encounters: " + foundPatient.getEncounters().size());
    }
}
