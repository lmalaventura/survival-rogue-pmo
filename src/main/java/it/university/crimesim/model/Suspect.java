package it.university.crimesim.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class Suspect {

    private final String id;
    private final String name;
    private final String description;
    private final String motive;
    private final String alibi;
    private final Set<String> linkedEvidenceIds = new LinkedHashSet<>();

    public Suspect(String id, String name, String description, String motive, String alibi) {
        this.id = requireText(id, "id");
        this.name = requireText(name, "name");
        this.description = description == null ? "" : description;
        this.motive = motive == null ? "" : motive;
        this.alibi = alibi == null ? "" : alibi;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getMotive() {
        return motive;
    }

    public String getAlibi() {
        return alibi;
    }

    public Set<String> getLinkedEvidenceIds() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(linkedEvidenceIds));
    }

    public boolean hasLinkedEvidence(String evidenceId) {
        return linkedEvidenceIds.contains(evidenceId);
    }

    void addLinkedEvidence(Evidence evidence) {
        linkedEvidenceIds.add(Objects.requireNonNull(evidence).getId());
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " non puo essere vuoto.");
        }
        return value;
    }
}
