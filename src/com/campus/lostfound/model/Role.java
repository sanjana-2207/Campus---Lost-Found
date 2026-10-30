package com.campus.lostfound.model;

/** Roles control who may verify ownership claims. */
public enum Role {

    STUDENT("Student"),
    STAFF("Staff / Security"),
    ADMIN("Administrator");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
