package it.university.crimesim;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AppTest {

    @Test
    void appClassUsesBasePackage() {
        assertEquals("it.university.crimesim.App", App.class.getName());
    }
}
