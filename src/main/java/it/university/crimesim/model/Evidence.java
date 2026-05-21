package it.university.crimesim.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class Evidence {

    private final String id;
    private final String title;
    private final String description;
    private final EvidenceType type;
    private boolean discovered;
    private final Set<String> linkedSuspectIds = new LinkedHashSet<>();

    public Evidence(String id, String title, String description, EvidenceType type) {
        this.id = requireText(id, "id");
        this.title = requireText(title, "title");
        this.description = description == null ? "" : description;
        this.type = Objects.requireNonNull(type);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public EvidenceType getType() {
        return type;
    }

    public boolean isDiscovered() {
        return discovered;
    }

    public Set<String> getLinkedSuspectIds() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(linkedSuspectIds));
    }

    public void markDiscovered() {
        discovered = true;
    }

    public boolean linkToSuspect(Suspect suspect) {
        if (!discovered) {
            throw new IllegalStateException("La prova deve essere scoperta prima di collegarla a un sospetto.");
        }

        Suspect checkedSuspect = Objects.requireNonNull(suspect);
        boolean added = linkedSuspectIds.add(checkedSuspect.getId());
        if (added) {
            checkedSuspect.addLinkedEvidence(this);
        }
        return added;
    }

    public boolean isLinkedToSuspect(String suspectId) {
        return linkedSuspectIds.contains(suspectId);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " non puo essere vuoto.");
        }
        return value;
    }
}
