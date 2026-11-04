package com.campus.web.model;

public enum Role {
    STUDENT("Student"),
    STAFF("Staff"),
    ADMIN("Administrator");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
