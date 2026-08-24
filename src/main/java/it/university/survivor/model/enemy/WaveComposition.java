package it.university.survivor.model.enemy;

import java.util.List;
import java.util.Objects;

public record WaveComposition(List<EnemyWaveEntry> entries) {

    public WaveComposition {
        Objects.requireNonNull(entries, "Entries must not be null");

        if (entries.isEmpty()) {
            throw new IllegalArgumentException(
                    "Wave composition must contain at least one entry"
            );
        }

        entries = List.copyOf(entries);
    }

    public int totalEnemyCount() {
        return entries.stream()
                .mapToInt(EnemyWaveEntry::count)
                .sum();
    }
}