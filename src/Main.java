import java.io.InputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MM/dd/uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

    public static void main(String[] args) {
        run(System.in, System.out);
    }

    static void run(InputStream input, PrintStream output) {
        try (Scanner scanner = new Scanner(input)) {
            PatientRepository patients = new PatientRepository();
            boolean running = true;

            try {
                while (running) {
                    printMenu(output);
                    String selection = readRequired(scanner, output, "Select an option: ");

                    switch (selection) {
                        case "1" -> registerPatient(scanner, output, patients);
                        case "2" -> findPatient(scanner, output, patients);
                        case "3" -> listPatients(output, patients);
                        case "4" -> recordEncounter(scanner, output, patients);
                        case "5" -> viewEncounters(scanner, output, patients);
                        case "6" -> running = false;
                        default -> output.println("Invalid selection. Please choose 1 through 6.");
                    }
                }
            } catch (EndOfInputException exception) {
                output.println("\nInput closed.");
            }
        }

        output.println("Horizon has closed.");
    }

    private static void printMenu(PrintStream output) {
        output.println("\nHorizon Healthcare Information System");
        output.println("1. Register a patient");
        output.println("2. Find patient by MRN");
        output.println("3. List all patients");
        output.println("4. Record a patient encounter");
        output.println("5. View patient encounters");
        output.println("6. Exit");
    }

    private static void registerPatient(Scanner scanner, PrintStream output,
                                        PatientRepository patients) {
        String mrn;
        while (true) {
            mrn = readRequired(scanner, output, "Medical record number: ");
            if (patients.findByMedicalRecordNumber(mrn).isEmpty()) {
                break;
            }
            output.println("That medical record number already exists. Please try again.");
        }

        String fullName = readRequired(scanner, output, "Full name: ");
        String dateInput = readRequired(scanner, output, "Date of birth (MM/DD/YYYY): ");

        try {
            LocalDate dateOfBirth = parseDate(dateInput);
            String email = readRequired(scanner, output, "Email: ");
            Patient patient = new Patient(mrn, fullName, dateOfBirth, email);
            patients.register(patient);
            output.println("Patient registered successfully.");
            output.println("MRN: " + patient.getMedicalRecordNumber());
        } catch (DateTimeParseException exception) {
            output.println("Invalid date. Please use a real date in MM/DD/YYYY format.");
        } catch (IllegalArgumentException exception) {
            output.println("Registration failed: " + exception.getMessage());
        }
    }

    private static void findPatient(Scanner scanner, PrintStream output,
                                    PatientRepository patients) {
        String mrn = readRequired(scanner, output, "Medical record number: ");
        patients.findByMedicalRecordNumber(mrn).ifPresentOrElse(
                patient -> printPatient(output, patient),
                () -> output.println("No patient found with MRN " + mrn + "."));
    }

    private static void listPatients(PrintStream output, PatientRepository patients) {
        List<Patient> allPatients = patients.findAll();
        if (allPatients.isEmpty()) {
            output.println("No patients are registered.");
            return;
        }

        output.println("Registered patients:");
        allPatients.forEach(patient -> printPatient(output, patient));
    }

    private static void recordEncounter(Scanner scanner, PrintStream output,
                                        PatientRepository patients) {
        String mrn = readRequired(scanner, output, "Medical record number: ");
        Patient patient = patients.findByMedicalRecordNumber(mrn).orElse(null);
        if (patient == null) {
            output.println("No patient found with MRN " + mrn + ".");
            return;
        }

        String dateInput = readRequired(scanner, output, "Encounter date (MM/DD/YYYY): ");
        String type = readRequired(scanner, output, "Encounter type: ");
        String clinician = readRequired(scanner, output, "Clinician: ");
        output.print("Notes (optional): ");
        String notes = readLine(scanner);

        try {
            patient.recordEncounter(new Encounter(parseDate(dateInput), type, clinician, notes));
            output.println("Encounter recorded successfully.");
        } catch (DateTimeParseException exception) {
            output.println("Invalid date. Please use a real date in MM/DD/YYYY format.");
        } catch (IllegalArgumentException exception) {
            output.println("Encounter could not be recorded: " + exception.getMessage());
        }
    }

    private static void viewEncounters(Scanner scanner, PrintStream output,
                                       PatientRepository patients) {
        String mrn = readRequired(scanner, output, "Medical record number: ");
        Patient patient = patients.findByMedicalRecordNumber(mrn).orElse(null);
        if (patient == null) {
            output.println("No patient found with MRN " + mrn + ".");
            return;
        }

        List<Encounter> encounters = patient.getEncounters();
        if (encounters.isEmpty()) {
            output.println("No encounters recorded for " + patient.getFullName() + ".");
            return;
        }

        output.println("Encounters for " + patient.getFullName() + ":");
        for (Encounter encounter : encounters) {
            output.println(DATE_FORMATTER.format(encounter.date()) + " | "
                    + encounter.type() + " | " + encounter.clinician());
            if (!encounter.notes().isBlank()) {
                output.println("Notes: " + encounter.notes());
            }
        }
    }

    private static void printPatient(PrintStream output, Patient patient) {
        output.println(patient.summary() + " | Email: " + patient.getEmail());
    }

    private static LocalDate parseDate(String value) {
        return LocalDate.parse(value, DATE_FORMATTER);
    }

    private static String readRequired(Scanner scanner, PrintStream output, String prompt) {
        while (true) {
            output.print(prompt);
            String value = readLine(scanner).trim();
            if (!value.isBlank()) {
                return value;
            }
            output.println("This field is required. Please enter a value.");
        }
    }

    private static String readLine(Scanner scanner) {
        if (!scanner.hasNextLine()) {
            throw new EndOfInputException();
        }
        return scanner.nextLine();
    }

    private static final class EndOfInputException extends RuntimeException {
    }
}
