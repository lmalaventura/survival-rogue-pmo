package it.university.survivor;

import javafx.application.Application;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {

    @Test
    void appIsAJavaFxApplication() {
        assertTrue(Application.class.isAssignableFrom(App.class));
    }
}
