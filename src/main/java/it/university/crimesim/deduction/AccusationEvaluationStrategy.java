package it.university.crimesim.deduction;

import it.university.crimesim.model.Accusation;
import it.university.crimesim.model.CaseFile;
import it.university.crimesim.model.EvaluationResult;

public interface AccusationEvaluationStrategy {

    EvaluationResult evaluate(CaseFile caseFile, Accusation accusation);
}
