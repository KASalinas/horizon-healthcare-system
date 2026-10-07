import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

public class HealthcareSystemTest {
    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        runTest("search finds a registered patient", HealthcareSystemTest::searchFindsPatient);
        runTest("search reports an unknown patient", HealthcareSystemTest::searchReportsUnknownPatient);
        runTest("listing preserves registration order", HealthcareSystemTest::listingPreservesOrder);
        runTest("encounters can be recorded and viewed", HealthcareSystemTest::encountersCanBeRecordedAndViewed);
        runTest("duplicate MRNs are rejected", HealthcareSystemTest::rejectsDuplicateMedicalRecordNumber);
        runTest("impossible birth dates are rejected", HealthcareSystemTest::rejectsImpossibleBirthDate);
        runTest("impossible encounter dates are rejected", HealthcareSystemTest::rejectsImpossibleEncounterDate);
        runTest("missing menu input closes gracefully", HealthcareSystemTest::missingMenuInputClosesGracefully);
        runTest("missing workflow input closes gracefully", HealthcareSystemTest::missingWorkflowInputClosesGracefully);

        System.out.println("Tests passed: " + passed);
        System.out.println("Tests failed: " + failed);
        if (failed > 0) {
            throw new AssertionError(failed + " test(s) failed");
        }
    }

    private static void searchFindsPatient() {
        String output = runApplication("1\nMRN-2001\nTaylor Morgan\n06/20/1990\n"
                + "taylor@example.test\n2\nMRN-2001\n6\n");
        check(output.contains("MRN-2001 | Taylor Morgan | DOB: 1990-06-20 | Email: taylor@example.test"),
                "Search should display the matching patient");
    }

    private static void searchReportsUnknownPatient() {
        String output = runApplication("2\nUNKNOWN\n6\n");
        check(output.contains("No patient found with MRN UNKNOWN."),
                "Search should report an unknown MRN");
    }

    private static void listingPreservesOrder() {
        PatientRepository repository = new PatientRepository();
        Patient first = samplePatient("MRN-2001", "Taylor Morgan");
        Patient second = samplePatient("MRN-2002", "Jordan Lee");
        repository.register(first);
        repository.register(second);
        check(repository.findAll().equals(List.of(first, second)),
                "Listing should contain every patient in registration order");

        String output = runApplication("1\nMRN-1\nFirst Patient\n01/01/1990\nfirst@example.test\n"
                + "1\nMRN-2\nSecond Patient\n02/02/1992\nsecond@example.test\n3\n6\n");
        check(output.indexOf("MRN-1 | First Patient") < output.indexOf("MRN-2 | Second Patient"),
                "CLI listing should preserve registration order");
    }

    private static void encountersCanBeRecordedAndViewed() {
        Patient patient = samplePatient("MRN-2001", "Taylor Morgan");
        patient.recordEncounter(new Encounter(
                LocalDate.of(2025, 1, 15), "Urgent care", "Dr. Patel", "Follow-up recommended"));
        check(patient.getEncounters().size() == 1, "Patient should retain the encounter");

        String output = runApplication("1\nMRN-2001\nTaylor Morgan\n06/20/1990\n"
                + "taylor@example.test\n4\nMRN-2001\n01/15/2025\nUrgent care\n"
                + "Dr. Patel\nFollow-up recommended\n5\nMRN-2001\n6\n");
        check(output.contains("Encounter recorded successfully."),
                "CLI should confirm encounter recording");
        check(output.contains("01/15/2025 | Urgent care | Dr. Patel"),
                "CLI should display encounter details");
        check(output.contains("Notes: Follow-up recommended"),
                "CLI should display encounter notes");
    }

    private static void rejectsDuplicateMedicalRecordNumber() {
        PatientRepository repository = new PatientRepository();
        repository.register(samplePatient("MRN-2001", "Taylor Morgan"));
        try {
            repository.register(samplePatient("MRN-2001", "Another Patient"));
            throw new AssertionError("Duplicate MRN should be rejected");
        } catch (IllegalArgumentException expected) {
            check(expected.getMessage().contains("already exists"),
                    "Duplicate error should explain the problem");
        }
    }

    private static void rejectsImpossibleBirthDate() {
        String output = runApplication("1\nMRN-2001\nTaylor Morgan\n02/30/2020\n6\n");
        check(output.contains("Invalid date."), "Impossible birth date should be rejected");
        check(!output.contains("Patient registered successfully."),
                "Patient with impossible birth date should not be registered");
    }

    private static void rejectsImpossibleEncounterDate() {
        String output = runApplication("1\nMRN-2001\nTaylor Morgan\n06/20/1990\n"
                + "taylor@example.test\n4\nMRN-2001\n02/30/2020\nCheckup\nDr. Patel\n\n6\n");
        check(output.contains("Invalid date."), "Impossible encounter date should be rejected");
        check(!output.contains("Encounter recorded successfully."),
                "Impossible encounter should not be recorded");
    }

    private static void missingMenuInputClosesGracefully() {
        String output = runApplication("");
        check(output.contains("Input closed."), "End-of-input should be acknowledged");
        check(output.endsWith("Horizon has closed.\n"), "Application should close normally");
    }

    private static void missingWorkflowInputClosesGracefully() {
        String output = runApplication("1\nMRN-2001\n");
        check(output.contains("Input closed."), "Missing workflow input should be acknowledged");
        check(output.endsWith("Horizon has closed.\n"), "Application should close normally");
    }

    private static String runApplication(String input) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
                input.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Main.run(inputStream, new PrintStream(outputStream, true, StandardCharsets.UTF_8));
        return outputStream.toString(StandardCharsets.UTF_8);
    }

    private static Patient samplePatient(String mrn, String name) {
        return new Patient(mrn, name, LocalDate.of(1990, 6, 20),
                "patient@example.test");
    }

    private static void runTest(String name, Runnable test) {
        try {
            test.run();
            passed++;
            System.out.println("PASS: " + name);
        } catch (Throwable exception) {
            failed++;
            System.out.println("FAIL: " + name + " - " + exception.getMessage());
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
