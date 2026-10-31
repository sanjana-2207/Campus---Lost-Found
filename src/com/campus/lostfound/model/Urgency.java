package com.campus.lostfound.model;

/** How quickly the owner needs the lost item back. */
public enum Urgency {

    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High");

    private final String label;

    Urgency(String label) {
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
