package it.university.survivor;

import it.university.survivor.controller.GameController;
import it.university.survivor.controller.MovementDirection;
import it.university.survivor.model.Enemy;
import it.university.survivor.model.ExperienceProgression;
import it.university.survivor.model.GameWorld;
import it.university.survivor.model.Player;
import it.university.survivor.model.Position;
import it.university.survivor.model.RunStatistics;
import it.university.survivor.model.enemy.Wave;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GameLoopTest {

    @Test
    void pausedRuntimeSkipsSimulationAndElapsedTimeUntilResumed() {
        Player player = new Player(new Position(50.0, 50.0), 100, 100.0);
        Enemy enemy = new Enemy(new Position(180.0, 180.0), 100, 1.0);
        GameWorld world = new GameWorld(
                200.0,
                200.0,
                player,
                List.of(enemy)
        );
        RunStatistics statistics = new RunStatistics();
        GameController controller = new GameController(
                world,
                new ExperienceProgression(),
                statistics,
                Map.of(),
                new Wave(1, List.of(enemy))
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
