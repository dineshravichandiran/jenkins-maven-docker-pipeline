package dev.dinesh.greeter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GreeterTest {

    @Test
    void greetsByName() {
        assertEquals("Hello, Dinesh!", Greeter.greet("Dinesh"));
    }

    @Test
    void fallsBackToWorldWhenMissingOrBlank() {
        assertEquals("Hello, world!", Greeter.greet(null));
        assertEquals("Hello, world!", Greeter.greet("   "));
    }

    @Test
    void dropsUnsafeCharacters() {
        assertEquals("Hello, scriptalert1script!", Greeter.greet("<script>alert(1)</script>"));
    }

    @Test
    void truncatesLongNames() {
        String greeting = Greeter.greet("a".repeat(100));
        assertEquals("Hello, ".length() + 40 + "!".length(), greeting.length());
        assertTrue(greeting.startsWith("Hello, aaaa"));
    }
}
