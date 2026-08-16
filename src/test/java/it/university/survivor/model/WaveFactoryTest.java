package it.university.survivor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import it.university.survivor.model.enemy.Wave;
import it.university.survivor.model.enemy.WaveFactory;

class WaveFactoryTest {

    private List<Position> createPositions(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> new Position(i * 100.0, 100.0))
                .toList();
    }

    @Test
    void shouldCreateWaveOneWithCorrectConfiguration() {
        Wave wave = WaveFactory.createWave(
                1,
                createPositions(3)
        );

        assertEquals(1, wave.getWaveNumber());
        assertEquals(3, wave.getEnemies().size());

        assertEquals(100, wave.getEnemies().get(0).getHealth().getMaxHealth());
        assertEquals(80.0, wave.getEnemies().get(0).getMovementSpeed());
    }

    @Test
    void shouldCreateWaveTwoWithCorrectConfiguration() {
        Wave wave = WaveFactory.createWave(
                2,
                createPositions(4)
        );

        assertEquals(2, wave.getWaveNumber());
        assertEquals(4, wave.getEnemies().size());

        assertEquals(110, wave.getEnemies().get(0).getHealth().getMaxHealth());
        assertEquals(82.0, wave.getEnemies().get(0).getMovementSpeed());
    }

    @Test
    void shouldCreateWaveFiveWithCorrectConfiguration() {
        Wave wave = WaveFactory.createWave(
                5,
                createPositions(7)
        );

        assertEquals(5, wave.getWaveNumber());
        assertEquals(7, wave.getEnemies().size());

        assertEquals(140, wave.getEnemies().get(0).getHealth().getMaxHealth());
        assertEquals(88.0, wave.getEnemies().get(0).getMovementSpeed());
    }

    @Test
    void shouldRejectNullSpawnPositions() {
        assertThrows(
                NullPointerException.class,
                () -> WaveFactory.createWave(1, null)
        );
    }

    @Test
    void shouldRejectWrongNumberOfSpawnPositions() {
        assertThrows(
                IllegalArgumentException.class,
                () -> WaveFactory.createWave(
                        1,
                        createPositions(2)
                )
        );
    }

    @Test
    void shouldRejectTooManySpawnPositions() {
        assertThrows(
                IllegalArgumentException.class,
                () -> WaveFactory.createWave(
                        1,
                        createPositions(4)
                )
        );
    }

    @Test
    void shouldRejectInvalidWaveNumber() {
        assertThrows(
                IllegalArgumentException.class,
                () -> WaveFactory.createWave(
                        0,
                        createPositions(3)
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> WaveFactory.createWave(
                        -1,
                        createPositions(3)
                )
        );
    }
}