import java.time.LocalDate;
import java.util.Scanner;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        DateTimeFormatter dateFormatter =
                DateTimeFormatter.ofPattern("MM/dd/uuuu");
        PatientRepository patients = new PatientRepository();

        boolean running = true;

        while (running) {
            System.out.println("\nHorizon Healthcare Information System");
            System.out.println("1. Register a patient");
            System.out.println("2. Find patient by MRN");
            System.out.println("3. List all patients");
            System.out.println("4. Exit");
            System.out.print("Select an option: ");

            String selection = scanner.nextLine();


            switch (selection) {
                case "1": {
                    String mrn;
                    while (true) {
                        mrn = readRequired(
                                scanner, "Medical record number: ");

                        if (patients.findByMedicalRecordNumber(mrn).isEmpty()) {
                            break;
                        }

                        System.out.println(
                                "That medical record number already exists. Please try again.");
                    }

                    String fullName = readRequired(
                            scanner, "Full name: ");

                    String dateOfBirthInput = readRequired(
                            scanner, "Date of birth (MM/DD/YYYY): ");

                    try {
                        LocalDate dateOfBirth =
                                LocalDate.parse(dateOfBirthInput, dateFormatter);

                        String email = readRequired(
                                scanner, "Email: ");

                        Patient patient = new Patient(mrn, fullName, dateOfBirth, email);
                        patients.register(patient);

                        System.out.println("Patient registered successfully.");
                        System.out.println("MRN: " + patient.getMedicalRecordNumber());
                    } catch (DateTimeParseException exception) {
                        System.out.println(
                                "Invalid date. Please use MM/DD/YYYY.");
                    }
                    catch (IllegalArgumentException exception) {
                        System.out.println(
                                "Registration failed: " + exception.getMessage());
                    }
                    break;
                }
                case "2":
                    System.out.println("Patient search selected.");
                    break;
                case "3":
                    System.out.println("Patient list selected.");
                    break;
                case "4":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid selection. Please choose 1 through 4.");
            }
        }
        scanner.close();
        System.out.println("Horizon has closed.");

    } // main ends here

    private static String readRequired(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();

            if (!value.isBlank()) {
                return value;
            }

            System.out.println(
                    "This field is required. Please enter a value.");
        }

    }
}//Main class ends here
