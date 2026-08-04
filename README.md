# Divisione generale

| **Leonardo** | 

                Core della run, mondo, player, salute, aggiornamento, collisioni generiche e integrazione      | Responsabilità generale |
                
                Applicazione avviabile, arena, player mobile, confini, salute e ciclo di aggiornamento         | Prima implementazione |
                
                Non implementare AI specifica dei nemici, auto-aim, singole armi, item concreti o tutta la GUI | Cosa non deve fare |
                
                Arena, player, input, HUD della vita e coordinamento delle schermate                           | Parte GUI |
| **Matteo**   |

                Armi, targeting, cooldown, attacchi, proiettili, livelli ed evoluzioni                         | Responsabilità generale |
                
                Una sola arma base che seleziona il nemico più vicino e richiede la creazione di un proiettile | Prima implementazione |
                
                Non gestire movimento dei nemici, collisioni generali, wave, esperienza o ciclo della run      | Cosa non deve fare |
                
                Visualizzazione dell’arma, dei proiettili e informazioni sull’arma                             | Parte GUI |
| **Giada**    |

                Nemici, comportamento, movimento desiderato, spawn, wave, mini-boss e boss                           | Responsabilità generale |
                
                Un nemico base che calcola come avvicinarsi al player e una wave minima che lo genera                | Prima implementazione |
                
                Non gestire movimento del player, auto-aim delle armi, collisioni generali o stato globale della run | Cosa non deve fare |
                
                Visualizzazione dei nemici, indicatore della wave e in seguito barra del boss                        | Parte GUI |
| **Gioia**    |

                Esperienza, item, rarità, upgrade, reroll e statistiche finali                               | Responsabilità generale |
                
                Un item numerico applicabile e una schermata con tre opzioni, inizialmente anche predefinite | Prima implementazione |
                
                Non sviluppare tutta la GUI, il loop real-time, nemici, armi o architettura globale          | Cosa non deve fare |
                
                Schermata upgrade, visualizzazione item e schermata finale                                   | Parte GUI |

# Confini tra le parti

- Leonardo e Giada

Giada stabilisce come il nemico vuole muoversi:

direzione;
velocità;
distanza dal bersaglio;
eventuale condizione di attacco.

Leonardo gestisce l’applicazione generale dello spostamento:
aggiornamento della posizione;
tempo trascorso;
confini dell’arena;
collisioni.

In pratica:

Nemico: voglio muovermi in questa direzione.
Mondo: applico lo spostamento rispettando le regole generali.


- Leonardo e Matteo

Matteo gestisce:

scelta del bersaglio;
cooldown;
logica dell’arma;
caratteristiche del proiettile.

Leonardo gestisce:

inserimento del proiettile nel mondo;
aggiornamento della sua posizione;
rilevamento della collisione;
rimozione delle entità.
Arma: voglio generare questo proiettile verso questo bersaglio.
Mondo: aggiungo e aggiorno il proiettile.


- Leonardo e Gioia

Gioia stabilisce:

quale potenziamento viene applicato;
quale statistica modifica;
quale item viene ottenuto;
quando deve essere mostrata una scelta.

Leonardo coordina il passaggio generale della run:

Wave conclusa → fase di scelta → upgrade applicato → nuova wave.


- Matteo e Gioia

Gioia non modifica direttamente i campi interni delle armi.

Dovrà richiedere operazioni concettuali come:

aumentare il danno;
diminuire il cooldown;
aumentare la penetrazione;
far salire di livello un’arma.

Sarà Matteo a stabilire come l’arma rappresenta e applica questi cambiamenti.

# Prima consegna di ciascuno
# Leonardo

Deve ottenere:

progetto Maven Java 17;
JavaFX e JUnit configurati;
finestra con arena;
player rappresentato inizialmente da una forma;
movimento tramite tastiera;
player confinato nell’arena;
salute del player;
aggiornamento della simulazione;
struttura minima per aggiungere e rimuovere entità.

# Matteo

Deve ottenere una prima arma che:

conosce il proprio cooldown;
riceve la posizione del player;
riceve una collezione di bersagli validi;
seleziona quello più vicino;
calcola la direzione del tiro;
produce i dati necessari per creare un proiettile;
non utilizza direttamente JavaFX;
possiede test su cooldown e selezione del bersaglio.

Non deve ancora implementare quattro armi o le evoluzioni.

# Giada

Deve ottenere un primo nemico che:

possiede posizione, salute e velocità;
conosce la posizione del bersaglio;
calcola una direzione per avvicinarsi;
può subire danno;
può risultare sconfitto;
non modifica direttamente il mondo;
possiede test su movimento desiderato, salute e morte.

Inoltre può preparare una wave minimale:

Wave 1:
- genera tre nemici base;
- termina quando sono stati generati e sconfitti tutti.

Non deve ancora implementare élite, ranged, mini-boss e boss.

# Gioia

Deve ottenere:

una rappresentazione minima di un item;
una rarità;
un modificatore numerico;
applicazione dell’item a una statistica;
tre opzioni di upgrade mostrate in una schermata semplice;
selezione di un’opzione;
test sull’applicazione del modificatore.

Prima versione possibile:

Power Core comune: +5% danno
Power Core raro: +10% danno
Armor Module comune: +10 salute massima
Attack Module comune: -5% cooldown

Non deve ancora implementare casualità complessa, altari o effetti speciali.
                
