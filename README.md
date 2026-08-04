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
