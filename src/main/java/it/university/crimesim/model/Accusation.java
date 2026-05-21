package it.university.crimesim.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class Accusation {

    private final String suspectId;
    private final Set<String> evidenceIds;
    private final String explanation;

    public Accusation(String suspectId, Collection<String> evidenceIds, String explanation) {
        this.suspectId = requireText(suspectId, "suspectId");
        this.evidenceIds = new LinkedHashSet<>(Objects.requireNonNull(evidenceIds));
        this.explanation = explanation == null ? "" : explanation;
    }

    public String getSuspectId() {
        return suspectId;
    }

    public Set<String> getEvidenceIds() {
        return Collections.unmodifiableSet(evidenceIds);
    }

    public String getExplanation() {
        return explanation;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " non puo essere vuoto.");
        }
        return value;
    }
}
