package service.builder;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class SystemBuilder {
    private LocalDate date;

    public SystemBuilder() {
        // Default: Η σημερινή ημερομηνία συστήματος
        this.date = LocalDate.now();
    }

    // Αν έχουμε ήδη αντικείμενο LocalDate
    public SystemBuilder setDate(LocalDate date) {
        if (date != null) {
            this.date = date;
        }
        return this;
    }

    // Αν διαβάζουμε String (από CSV)
    public SystemBuilder setDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return this; // Κρατάμε το default (today)
        }
        try {
            this.date = LocalDate.parse(dateStr.trim());
        } catch (DateTimeParseException e) {
            System.err.println("Invalid date format in system file. Defaulting to NOW.");
            // Σε περίπτωση λάθους, κρατάμε το default (today) που ορίστηκε στον constructor
        }
        return this;
    }

    public LocalDate build() {
        return this.date;
    }
}