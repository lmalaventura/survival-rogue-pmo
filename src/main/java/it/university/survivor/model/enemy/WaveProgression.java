package it.university.survivor.model.enemy;

public final class WaveProgression {

    private WaveProgression() {
    }

    public static WaveConfig getConfig(int waveNumber) {
        if (waveNumber <= 0) {
            throw new IllegalArgumentException(
                    "Wave number must be greater than zero"
            );
        }

        int enemyCount = waveNumber + 2;
        int enemyHealth = 100 + (waveNumber - 1) * 10;
        double enemySpeed = 80.0 + (waveNumber - 1) * 2.0;

        return new WaveConfig(
                waveNumber,
                enemyCount,
                enemyHealth,
                enemySpeed
        );
    }
}