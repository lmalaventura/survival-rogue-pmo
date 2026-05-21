package it.university.crimesim.deduction;

import it.university.crimesim.model.Accusation;
import it.university.crimesim.model.CaseFile;
import it.university.crimesim.model.Evidence;
import it.university.crimesim.model.EvaluationResult;
import java.util.Objects;

public class StandardAccusationEvaluationStrategy implements AccusationEvaluationStrategy {

    @Override
    public EvaluationResult evaluate(CaseFile caseFile, Accusation accusation) {
        Objects.requireNonNull(caseFile);
        Objects.requireNonNull(accusation);

        int requiredCount = caseFile.getRequiredEvidenceIds().size();
        int matchedCount = countMatchedEvidence(caseFile, accusation);

        if (caseFile.findSuspectById(accusation.getSuspectId()).isEmpty()) {
            return new EvaluationResult(false, "Il sospetto accusato non fa parte del caso.", matchedCount, requiredCount);
        }

        if (!caseFile.getSolutionSuspectId().equals(accusation.getSuspectId())) {
            return new EvaluationResult(false, "Accusa non convincente: il sospetto scelto non e quello giusto.", matchedCount, requiredCount);
        }

        if (matchedCount < requiredCount) {
            return new EvaluationResult(false, "Accusa incompleta: mancano prove decisive.", matchedCount, requiredCount);
        }

        if (!requiredEvidenceIsUsable(caseFile, accusation)) {
            return new EvaluationResult(false, "Le prove decisive devono essere scoperte e collegate al sospetto.", matchedCount, requiredCount);
        }

        return new EvaluationResult(true, "Accusa corretta: prove e sospetto sono coerenti.", matchedCount, requiredCount);
    }

    private int countMatchedEvidence(CaseFile caseFile, Accusation accusation) {
        int matched = 0;
        for (String evidenceId : caseFile.getRequiredEvidenceIds()) {
            if (accusation.getEvidenceIds().contains(evidenceId)) {
                matched++;
            }
        }
        return matched;
    }

    private boolean requiredEvidenceIsUsable(CaseFile caseFile, Accusation accusation) {
        for (String evidenceId : caseFile.getRequiredEvidenceIds()) {
            Evidence evidence = caseFile.findEvidenceById(evidenceId)
                    .orElseThrow(() -> new IllegalStateException("Prova decisiva non presente nel caso: " + evidenceId));
            if (!evidence.isDiscovered() || !evidence.isLinkedToSuspect(accusation.getSuspectId())) {
                return false;
            }
        }
        return true;
    }
}
