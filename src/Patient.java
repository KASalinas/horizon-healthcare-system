import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Patient {
    private final String medicalRecordNumber;
    private String fullName;
    private LocalDate dateOfBirth;
    private String email;
    private final List<Encounter> encounters = new ArrayList<>();

    public Patient(String medicalRecordNumber, String fullName,
                   LocalDate dateOfBirth, String email) {
        this.medicalRecordNumber = requireText(medicalRecordNumber, "Medical record number");
        this.fullName = requireText(fullName, "Full name");
        this.dateOfBirth = validDateOfBirth(dateOfBirth);
        this.email = validEmail(email);
    }

    public String getMedicalRecordNumber() {
        return medicalRecordNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = requireText(fullName, "Full name");
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = validDateOfBirth(dateOfBirth);
    }

    private static LocalDate validDateOfBirth(LocalDate dateOfBirth) {
        Objects.requireNonNull(dateOfBirth, "Date of birth is required");
        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        }
        return dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = validEmail(email);
    }

    private static String validEmail(String email) {
        String value = requireText(email, "Email");
        if (!value.contains("@")) {
            throw new IllegalArgumentException("Email must contain @");
        }
        return value;
    }

    public void recordEncounter(Encounter encounter) {
        encounters.add(Objects.requireNonNull(encounter, "Encounter is required"));
    }

    public List<Encounter> getEncounters() {
        return List.copyOf(encounters);
    }

    public String summary() {
        return medicalRecordNumber + " | " + fullName + " | DOB: " + dateOfBirth;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }
}
