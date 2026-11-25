package dev.dinesh.greeter;

/** Pure logic kept separate from the HTTP layer so it is easy to test. */
public final class Greeter {

    private static final int MAX_NAME_LENGTH = 40;

    private Greeter() {
    }

    /**
     * Builds a greeting. Blank or missing names fall back to "world"; anything other than
     * letters, digits, spaces, dots, hyphens and underscores is dropped so the output is safe
     * to embed in a response; the name is cut to {@value #MAX_NAME_LENGTH} characters.
     */
    public static String greet(String name) {
        String cleaned = name == null ? "" : name.replaceAll("[^\\p{L}\\p{N} ._-]", "").strip();
        if (cleaned.isEmpty()) {
            cleaned = "world";
        }
        if (cleaned.length() > MAX_NAME_LENGTH) {
            cleaned = cleaned.substring(0, MAX_NAME_LENGTH);
        }
        return "Hello, " + cleaned + "!";
    }
}
