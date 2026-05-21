package it.university.crimesim.model;

public class EvaluationResult {

    private final boolean correct;
    private final String message;
    private final int matchedEvidenceCount;
    private final int requiredEvidenceCount;

    public EvaluationResult(boolean correct, String message, int matchedEvidenceCount, int requiredEvidenceCount) {
        this.correct = correct;
        this.message = message == null ? "" : message;
        this.matchedEvidenceCount = matchedEvidenceCount;
        this.requiredEvidenceCount = requiredEvidenceCount;
    }

    public boolean isCorrect() {
        return correct;
    }

    public String getMessage() {
        return message;
    }

    public int getMatchedEvidenceCount() {
        return matchedEvidenceCount;
    }

    public int getRequiredEvidenceCount() {
        return requiredEvidenceCount;
    }
}
