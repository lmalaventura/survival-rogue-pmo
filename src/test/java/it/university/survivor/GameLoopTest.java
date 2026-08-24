package it.university.survivor;

import it.university.survivor.controller.GameController;
import it.university.survivor.controller.MovementDirection;
import it.university.survivor.model.ExperienceProgression;
import it.university.survivor.model.GameWorld;
import it.university.survivor.model.Player;
import it.university.survivor.model.Position;
import it.university.survivor.model.RunStatistics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GameLoopTest {

    @Test
    void pausedRuntimeSkipsSimulationAndElapsedTimeUntilResumed() {
        Player player = new Player(new Position(50.0, 50.0), 100, 100.0);
        GameWorld world = new GameWorld(200.0, 200.0, player);
        RunStatistics statistics = new RunStatistics();
        GameController controller = new GameController(
                world,
                new ExperienceProgression(),
                statistics
        );
        controller.setDirectionActive(MovementDirection.RIGHT, true);

        GameLoop.advanceSimulation(controller, 0.1, true);

        assertAll(
                () -> assertEquals(new Position(50.0, 50.0), player.getPosition()),
                () -> assertEquals(0.0, statistics.getElapsedTime())
        );

        GameLoop.advanceSimulation(controller, 0.1, false);

        assertAll(
                () -> assertEquals(new Position(60.0, 50.0), player.getPosition()),
                () -> assertEquals(0.1, statistics.getElapsedTime())
        );
    }
}
