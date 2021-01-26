package xyz.foobar;

public enum DiffFieldReflection {
    CREATE("Create"),
    UPDATE("Update"),
    DELETE("Delete"),
    SAME("Same"),
    BOTH_NULL("Both Null");

    public final String label;

    DiffFieldReflection(String label) {
        this.label = label;
    }
}
