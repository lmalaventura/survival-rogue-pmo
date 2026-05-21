package it.university.crimesim.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class CaseFile {

    private final String id;
    private final String title;
    private final String description;
    private final String solutionSuspectId;
    private final Set<String> requiredEvidenceIds;
    private final List<Suspect> suspects = new ArrayList<>();
    private final List<Evidence> evidences = new ArrayList<>();
    private final Timeline timeline = new Timeline();

    public CaseFile(String id, String title, String description, String solutionSuspectId, Set<String> requiredEvidenceIds) {
        this.id = requireText(id, "id");
        this.title = requireText(title, "title");
        this.description = description == null ? "" : description;
        this.solutionSuspectId = requireText(solutionSuspectId, "solutionSuspectId");
        this.requiredEvidenceIds = new LinkedHashSet<>(Objects.requireNonNull(requiredEvidenceIds));
        if (this.requiredEvidenceIds.isEmpty()) {
            throw new IllegalArgumentException("Il caso deve avere almeno una prova decisiva.");
        }
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

    public List<Suspect> getSuspects() {
        return Collections.unmodifiableList(suspects);
    }

    public List<Evidence> getEvidences() {
        return Collections.unmodifiableList(evidences);
    }

    public Timeline getTimeline() {
        return timeline;
    }

    public String getSolutionSuspectId() {
        return solutionSuspectId;
    }

    public Set<String> getRequiredEvidenceIds() {
        return Collections.unmodifiableSet(requiredEvidenceIds);
    }

    public void addSuspect(Suspect suspect) {
        Objects.requireNonNull(suspect);
        if (findSuspectById(suspect.getId()).isPresent()) {
            throw new IllegalArgumentException("Sospetto gia presente: " + suspect.getId());
        }
        suspects.add(suspect);
    }

    public void addEvidence(Evidence evidence) {
        Objects.requireNonNull(evidence);
        if (findEvidenceById(evidence.getId()).isPresent()) {
            throw new IllegalArgumentException("Prova gia presente: " + evidence.getId());
        }
        evidences.add(evidence);
    }

    public void addTimelineEvent(TimelineEvent event) {
        timeline.addEvent(event);
    }

    public Optional<Suspect> findSuspectById(String suspectId) {
        return suspects.stream()
                .filter(suspect -> suspect.getId().equals(suspectId))
                .findFirst();
    }

    public Optional<Evidence> findEvidenceById(String evidenceId) {
        return evidences.stream()
                .filter(evidence -> evidence.getId().equals(evidenceId))
                .findFirst();
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " non puo essere vuoto.");
        }
        return value;
    }
}
