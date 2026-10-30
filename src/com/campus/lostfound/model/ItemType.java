package com.campus.lostfound.model;

/**
 * Discriminator for the Item hierarchy.
 * The constant name is stored directly in the {@code items.item_type} column.
 */
public enum ItemType {

    LOST("Lost"),
    FOUND("Found");

    private final String label;

    ItemType(String label) {
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
