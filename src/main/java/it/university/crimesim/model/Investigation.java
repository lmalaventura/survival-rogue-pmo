package it.university.crimesim.model;

import it.university.crimesim.deduction.DeductionEngine;
import it.university.crimesim.observer.InvestigationEvent;
import it.university.crimesim.observer.InvestigationObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Investigation {

    private final CaseFile caseFile;
    private final List<InvestigationObserver> observers = new ArrayList<>();
    private InvestigationStatus status = InvestigationStatus.OPEN;
    private Accusation lastAccusation;
    private EvaluationResult lastEvaluationResult;

    public Investigation(CaseFile caseFile) {
        this.caseFile = Objects.requireNonNull(caseFile);
    }

    public CaseFile getCaseFile() {
        return caseFile;
    }

    public InvestigationStatus getStatus() {
        return status;
    }

    public Accusation getLastAccusation() {
        return lastAccusation;
    }

    public EvaluationResult getLastEvaluationResult() {
        return lastEvaluationResult;
    }

    public void addObserver(InvestigationObserver observer) {
        observers.add(Objects.requireNonNull(observer));
    }

    public void removeObserver(InvestigationObserver observer) {
        observers.remove(observer);
    }

    public void discoverEvidence(String evidenceId) {
        requireOpen();
        Evidence evidence = caseFile.findEvidenceById(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Prova non trovata: " + evidenceId));

        if (!evidence.isDiscovered()) {
            evidence.markDiscovered();
            notifyObservers(InvestigationEvent.evidenceDiscovered(evidence));
        }
    }

    public void linkEvidenceToSuspect(String evidenceId, String suspectId) {
        requireOpen();
        Evidence evidence = caseFile.findEvidenceById(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Prova non trovata: " + evidenceId));
        Suspect suspect = caseFile.findSuspectById(suspectId)
                .orElseThrow(() -> new IllegalArgumentException("Sospetto non trovato: " + suspectId));

        if (evidence.linkToSuspect(suspect)) {
            notifyObservers(InvestigationEvent.evidenceLinked(evidence, suspect));
        }
    }

    public EvaluationResult formulateAccusation(Accusation accusation, DeductionEngine deductionEngine) {
        requireOpen();
        lastAccusation = Objects.requireNonNull(accusation);
        status = InvestigationStatus.ACCUSATION_SUBMITTED;
        lastEvaluationResult = Objects.requireNonNull(deductionEngine).evaluate(caseFile, accusation);
        status = InvestigationStatus.CLOSED;
        notifyObservers(InvestigationEvent.accusationEvaluated(accusation, lastEvaluationResult));
        return lastEvaluationResult;
    }

    private void notifyObservers(InvestigationEvent event) {
        for (InvestigationObserver observer : List.copyOf(observers)) {
            observer.onInvestigationEvent(event);
        }
    }

    private void requireOpen() {
        if (status != InvestigationStatus.OPEN) {
            throw new IllegalStateException("L'indagine non e piu aperta.");
        }
    }
}
