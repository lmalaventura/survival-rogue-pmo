package it.university.crimesim.deduction;

import it.university.crimesim.model.Accusation;
import it.university.crimesim.model.CaseFile;
import it.university.crimesim.model.EvaluationResult;
import java.util.Objects;

public class DeductionEngine {

    private final AccusationEvaluationStrategy strategy;

    public DeductionEngine(AccusationEvaluationStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy);
    }

    public EvaluationResult evaluate(CaseFile caseFile, Accusation accusation) {
        return strategy.evaluate(caseFile, accusation);
    }
}
