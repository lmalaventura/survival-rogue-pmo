package it.university.survivor.model;

import java.util.List;
import java.util.Objects;

public class UpgradeCatalog {
    public record Template(String name, StatModifier modifier) {
        public Template {
            Objects.requireNonNull(name, "Name must not be null");
            if(name.isBlank()) {
                throw new IllegalArgumentException("Name must not be blank");

            }
            Objects.requireNonNull(modifier, "Modifier must not be null");
        }
    }

    private final List<Template> templates;

    public UpgradeCatalog(){
        this(createDefaultTemplates());
    }

    public UpgradeCatalog(List<Template> templates) {
        Objects.requireNonNull(templates, "Templates list must not be null");
        if(templates.size() < 3) {
            throw new IllegalArgumentException("Catalog must contain at least 3 templates");

        }
        this.templates = List.copyOf(templates);

    }
    public List<Template> getTemplates() {
        return templates;
    }
    private static List<Template> createDefaultTemplates() {
        return List.of(
            new Template("Cuore di Pietra", new StatModifier(StatType.MAX_HEALTH, ModifierType.FLAT, 20.0)),
            new Template("Armatura Vitalizzante", new StatModifier(StatType.MAX_HEALTH, ModifierType.PERCENTAGE, 0.10)),
            new Template("Modulo Potenza", new StatModifier(StatType.DAMAGE, ModifierType.FLAT, 5.0)),
            new Template("Iniettore di Plasma", new StatModifier(StatType.COOLDOWN, ModifierType.PERCENTAGE, -0.05)),
            new Template("Circuito Sovralimentato", new StatModifier(StatType.COOLDOWN, ModifierType.PERCENTAGE, 0.10)),
            new Template("Raffreddamento Istantaneo", new StatModifier(StatType.COOLDOWN, ModifierType.FLAT, -0.10))
        );
    }
}
