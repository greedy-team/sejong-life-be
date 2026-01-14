package org.example.sejonglifebe.place.view;

public record Viewer(String type, String key) {
    public static Viewer user(String studentId) {
        return new Viewer("USER", studentId);
    }

    public static Viewer ipua(String hash) {
        return new Viewer("IPUA", hash);
    }
}
