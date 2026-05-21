package it.university.crimesim.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class TimelineEvent {

    private final String id;
    private final LocalDateTime occurredAt;
    private final String title;
    private final String description;
    private final String suspectId;

    public TimelineEvent(String id, LocalDateTime occurredAt, String title, String description, String suspectId) {
        this.id = requireText(id, "id");
        this.occurredAt = Objects.requireNonNull(occurredAt);
        this.title = requireText(title, "title");
        this.description = description == null ? "" : description;
        this.suspectId = suspectId == null || suspectId.isBlank() ? null : suspectId;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Optional<String> getSuspectId() {
        return Optional.ofNullable(suspectId);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " non puo essere vuoto.");
        }
        return value;
    }
}
