package it.university.crimesim.observer;

import it.university.crimesim.model.Accusation;
import it.university.crimesim.model.EvaluationResult;
import it.university.crimesim.model.Evidence;
import it.university.crimesim.model.Suspect;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class InvestigationEvent {

    private final InvestigationEventType type;
    private final String message;
    private final LocalDateTime occurredAt;
    private final Evidence evidence;
    private final Suspect suspect;
    private final Accusation accusation;
    private final EvaluationResult evaluationResult;

    private InvestigationEvent(
            InvestigationEventType type,
            String message,
            Evidence evidence,
            Suspect suspect,
            Accusation accusation,
            EvaluationResult evaluationResult
    ) {
        this.type = Objects.requireNonNull(type);
        this.message = message == null ? "" : message;
        this.occurredAt = LocalDateTime.now();
        this.evidence = evidence;
        this.suspect = suspect;
        this.accusation = accusation;
        this.evaluationResult = evaluationResult;
    }

    public static InvestigationEvent evidenceDiscovered(Evidence evidence) {
        return new InvestigationEvent(
                InvestigationEventType.EVIDENCE_DISCOVERED,
                "Prova scoperta: " + evidence.getTitle(),
                Objects.requireNonNull(evidence),
                null,
                null,
                null
        );
    }

    public static InvestigationEvent evidenceLinked(Evidence evidence, Suspect suspect) {
        return new InvestigationEvent(
                InvestigationEventType.EVIDENCE_LINKED_TO_SUSPECT,
                "Prova collegata a " + suspect.getName() + ".",
                Objects.requireNonNull(evidence),
                Objects.requireNonNull(suspect),
                null,
                null
        );
    }

    public static InvestigationEvent accusationEvaluated(Accusation accusation, EvaluationResult result) {
        return new InvestigationEvent(
                InvestigationEventType.ACCUSATION_EVALUATED,
                result.getMessage(),
                null,
                null,
                Objects.requireNonNull(accusation),
                Objects.requireNonNull(result)
        );
    }

    public InvestigationEventType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public Optional<Evidence> getEvidence() {
        return Optional.ofNullable(evidence);
    }

    public Optional<Suspect> getSuspect() {
        return Optional.ofNullable(suspect);
    }

    public Optional<Accusation> getAccusation() {
        return Optional.ofNullable(accusation);
    }

    public Optional<EvaluationResult> getEvaluationResult() {
        return Optional.ofNullable(evaluationResult);
    }
}
