package it.university.crimesim.factory;

import it.university.crimesim.model.CaseFile;
import it.university.crimesim.model.Evidence;
import it.university.crimesim.model.EvidenceType;
import it.university.crimesim.model.Suspect;
import it.university.crimesim.model.TimelineEvent;
import java.time.LocalDateTime;
import java.util.Set;

public class CaseFactory {

    public CaseFile createDemoCase() {
        CaseFile caseFile = new CaseFile(
                "case-demo-001",
                "Il registro mancante",
                "Un responsabile amministrativo scompare dopo aver scoperto movimenti sospetti nei conti aziendali.",
                "sus-marta-greco",
                Set.of("ev-server-log", "ev-fingerprint")
        );

        caseFile.addSuspect(new Suspect(
                "sus-marta-greco",
                "Marta Greco",
                "Direttrice finanziaria con accesso ai registri interni.",
                "Nascondere una frode contabile prima dell'audit.",
                "Dice di essere rimasta in videochiamata da casa."
        ));
        caseFile.addSuspect(new Suspect(
                "sus-luca-conti",
                "Luca Conti",
                "Ex socio allontanato dalla societa.",
                "Vendicarsi dopo la perdita delle quote.",
                "Sostiene di essere stato fuori citta."
        ));

        caseFile.addEvidence(new Evidence(
                "ev-server-log",
                "Accesso al server",
                "Un accesso amministratore parte dal portatile di Marta poco prima della scomparsa.",
                EvidenceType.DIGITAL
        ));
        caseFile.addEvidence(new Evidence(
                "ev-fingerprint",
                "Impronta parziale",
                "Una traccia compatibile con Marta viene trovata sul registro cartaceo.",
                EvidenceType.PHYSICAL
        ));
        caseFile.addEvidence(new Evidence(
                "ev-guard-note",
                "Nota della guardia",
                "La guardia notturna segnala una donna con cappotto grigio vicino agli uffici.",
                EvidenceType.TESTIMONY
        ));

        caseFile.addTimelineEvent(new TimelineEvent(
                "tl-audit",
                LocalDateTime.of(2026, 3, 4, 18, 0),
                "Audit annunciato",
                "La vittima comunica che controllera i registri contabili.",
                null
        ));
        caseFile.addTimelineEvent(new TimelineEvent(
                "tl-login",
                LocalDateTime.of(2026, 3, 4, 22, 7),
                "Accesso sospetto",
                "Il server registra un accesso privilegiato.",
                "sus-marta-greco"
        ));
        caseFile.addTimelineEvent(new TimelineEvent(
                "tl-disappearance",
                LocalDateTime.of(2026, 3, 4, 22, 40),
                "Scomparsa denunciata",
                "La sicurezza non trova piu la vittima nel suo ufficio.",
                null
        ));

        return caseFile;
    }
}
