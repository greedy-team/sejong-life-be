package org.example.sejonglifebe.user;

public enum Role {
    USER(0),
    ADMIN(1);

    private final int level;

    Role(int level) {
        this.level = level;
    }

    public boolean includes(Role other) {
        return this.level >= other.level;
    }

    public static Role fromString(String value) {
        if (value == null) {
            return USER;
        }
        return valueOf(value);
    }
}
