package it.university.survivor.model;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UpgradeCatalogTest {

    @Test
    void defaultCatalogIsValid() {
        UpgradeCatalog catalog = new UpgradeCatalog();
        assertNotNull(catalog.getTemplates());
        int templateCount = catalog.getTemplates().size();
        assertTrue(templateCount >= 8 && templateCount <= 10);

        for (UpgradeCatalog.Template template : catalog.getTemplates()) {
            assertNotNull(template.name());
            assertFalse(template.name().isBlank());
            assertNotNull(template.modifier());
        }
    }

    @Test
    void rejectsCatalogWithLessThanThreeTemplates() {
        List<UpgradeCatalog.Template> smallList = List.of(
                new UpgradeCatalog.Template("Item1", new StatModifier(StatType.DAMAGE, ModifierType.FLAT, 5.0)),
                new UpgradeCatalog.Template("Item2", new StatModifier(StatType.MAX_HEALTH, ModifierType.FLAT, 10.0))
        );

        assertThrows(IllegalArgumentException.class, () -> new UpgradeCatalog(smallList));
    }

}
