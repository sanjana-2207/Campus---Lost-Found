package com.campus.lostfound.model;

/**
 * Life cycle of a reported item.
 *
 * <pre>
 * OPEN ──► MATCHED ──► CLAIMED ──► RECOVERED ──► CLOSED
 *   └───────────────────────────────────────────► (either report may be closed)
 * </pre>
 */
public enum ItemStatus {

    OPEN("Open"),
    MATCHED("Matched"),
    CLAIMED("Claimed"),
    RECOVERED("Recovered"),
    CLOSED("Closed");

    private final String label;

    ItemStatus(String label) {
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
