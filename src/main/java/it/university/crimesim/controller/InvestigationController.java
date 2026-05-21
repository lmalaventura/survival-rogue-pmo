package it.university.crimesim.controller;

import it.university.crimesim.deduction.DeductionEngine;
import it.university.crimesim.deduction.StandardAccusationEvaluationStrategy;
import it.university.crimesim.factory.CaseFactory;
import it.university.crimesim.model.Accusation;
import it.university.crimesim.model.CaseFile;
import it.university.crimesim.model.EvaluationResult;
import it.university.crimesim.model.Investigation;
import it.university.crimesim.observer.InvestigationObserver;
import java.util.Collection;
import java.util.Objects;

public class InvestigationController {

    private final Investigation investigation;
    private final DeductionEngine deductionEngine;

    public InvestigationController() {
        this(new CaseFactory().createDemoCase());
    }

    public InvestigationController(CaseFile caseFile) {
        this(new Investigation(caseFile), new DeductionEngine(new StandardAccusationEvaluationStrategy()));
    }

    public InvestigationController(Investigation investigation, DeductionEngine deductionEngine) {
        this.investigation = Objects.requireNonNull(investigation);
        this.deductionEngine = Objects.requireNonNull(deductionEngine);
    }

    public CaseFile getCaseFile() {
        return investigation.getCaseFile();
    }

    public Investigation getInvestigation() {
        return investigation;
    }

    public void addObserver(InvestigationObserver observer) {
        investigation.addObserver(observer);
    }

    public void discoverEvidence(String evidenceId) {
        investigation.discoverEvidence(evidenceId);
    }

    public void linkEvidenceToSuspect(String evidenceId, String suspectId) {
        investigation.linkEvidenceToSuspect(evidenceId, suspectId);
    }

    public EvaluationResult formulateAccusation(String suspectId, Collection<String> evidenceIds, String explanation) {
        return investigation.formulateAccusation(new Accusation(suspectId, evidenceIds, explanation), deductionEngine);
    }
}
