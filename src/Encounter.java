import java.time.LocalDate;
import java.util.Objects;

public record Encounter(LocalDate date, String type, String clinician, String notes) {
    public Encounter {
        Objects.requireNonNull(date, "Encounter date is required");
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Encounter date cannot be in the future");
        }
        type = requireText(type, "Encounter type");
        clinician = requireText(clinician, "Clinician");
        notes = notes == null ? "" : notes.trim();
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }
}
