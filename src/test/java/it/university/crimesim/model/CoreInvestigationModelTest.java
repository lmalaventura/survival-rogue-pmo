package it.university.crimesim.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.university.crimesim.deduction.DeductionEngine;
import it.university.crimesim.deduction.StandardAccusationEvaluationStrategy;
import it.university.crimesim.factory.CaseFactory;
import it.university.crimesim.observer.InvestigationEvent;
import it.university.crimesim.observer.InvestigationEventType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CoreInvestigationModelTest {

    private final CaseFactory caseFactory = new CaseFactory();

    @Test
    void createsDemoCase() {
        CaseFile caseFile = caseFactory.createDemoCase();

        assertEquals("case-demo-001", caseFile.getId());
        assertEquals("Il registro mancante", caseFile.getTitle());
        assertEquals(2, caseFile.getSuspects().size());
        assertEquals(3, caseFile.getEvidences().size());
        assertEquals(3, caseFile.getTimeline().getEvents().size());
        assertEquals("sus-marta-greco", caseFile.getSolutionSuspectId());
    }

    @Test
    void discoversEvidence() {
        Investigation investigation = new Investigation(caseFactory.createDemoCase());

        investigation.discoverEvidence("ev-server-log");

        Evidence evidence = investigation.getCaseFile().findEvidenceById("ev-server-log").orElseThrow();
        assertTrue(evidence.isDiscovered());
    }

    @Test
    void linksEvidenceToSuspect() {
        Investigation investigation = new Investigation(caseFactory.createDemoCase());

        investigation.discoverEvidence("ev-fingerprint");
        investigation.linkEvidenceToSuspect("ev-fingerprint", "sus-marta-greco");

        Evidence evidence = investigation.getCaseFile().findEvidenceById("ev-fingerprint").orElseThrow();
        Suspect suspect = investigation.getCaseFile().findSuspectById("sus-marta-greco").orElseThrow();
        assertTrue(evidence.isLinkedToSuspect("sus-marta-greco"));
        assertTrue(suspect.hasLinkedEvidence("ev-fingerprint"));
    }

    @Test
    void evaluatesCorrectAccusation() {
        Investigation investigation = new Investigation(caseFactory.createDemoCase());
        DeductionEngine deductionEngine = new DeductionEngine(new StandardAccusationEvaluationStrategy());

        investigation.discoverEvidence("ev-server-log");
        investigation.discoverEvidence("ev-fingerprint");
        investigation.linkEvidenceToSuspect("ev-server-log", "sus-marta-greco");
        investigation.linkEvidenceToSuspect("ev-fingerprint", "sus-marta-greco");

        EvaluationResult result = investigation.formulateAccusation(
                new Accusation("sus-marta-greco", Set.of("ev-server-log", "ev-fingerprint"), "Le prove portano a Marta."),
                deductionEngine
        );

        assertTrue(result.isCorrect());
        assertEquals(2, result.getMatchedEvidenceCount());
        assertEquals(InvestigationStatus.CLOSED, investigation.getStatus());
    }

    @Test
    void rejectsAccusationWithoutRequiredEvidence() {
        Investigation investigation = new Investigation(caseFactory.createDemoCase());
        DeductionEngine deductionEngine = new DeductionEngine(new StandardAccusationEvaluationStrategy());

        EvaluationResult result = investigation.formulateAccusation(
                new Accusation("sus-marta-greco", Set.of("ev-guard-note"), "La testimonianza sembra sospetta."),
                deductionEngine
        );

        assertFalse(result.isCorrect());
        assertEquals(0, result.getMatchedEvidenceCount());
    }

    @Test
    void notifiesObserver() {
        Investigation investigation = new Investigation(caseFactory.createDemoCase());
        DeductionEngine deductionEngine = new DeductionEngine(new StandardAccusationEvaluationStrategy());
        List<InvestigationEvent> events = new ArrayList<>();
        investigation.addObserver(events::add);

        investigation.discoverEvidence("ev-server-log");
        investigation.linkEvidenceToSuspect("ev-server-log", "sus-marta-greco");
        investigation.formulateAccusation(
                new Accusation("sus-marta-greco", Set.of("ev-server-log"), "Il log indica Marta."),
                deductionEngine
        );

        assertEquals(3, events.size());
        assertEquals(InvestigationEventType.EVIDENCE_DISCOVERED, events.get(0).getType());
        assertEquals(InvestigationEventType.EVIDENCE_LINKED_TO_SUSPECT, events.get(1).getType());
        assertEquals(InvestigationEventType.ACCUSATION_EVALUATED, events.get(2).getType());
        assertTrue(events.get(2).getEvaluationResult().isPresent());
    }
}
