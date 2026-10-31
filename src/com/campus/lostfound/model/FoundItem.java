package com.campus.lostfound.model;

import java.time.LocalDate;

/**
 * Concrete item reported by the person who FOUND it.
 *
 * <p>Adds physical storage information so the owner knows where to collect the
 * item once the claim is approved.</p>
 */
public class FoundItem extends Item {

    private String storageLocation;
    private boolean handedToSecurity = true;
    private String finderNotes;

    public FoundItem() {
        super();
    }

    public FoundItem(String title, String description, Category category,
                     Location location, LocalDate eventDate, User reportedBy,
                     String storageLocation, boolean handedToSecurity, String finderNotes) {
        super(title, description, category, location, eventDate, reportedBy);
        this.storageLocation = storageLocation;
        this.handedToSecurity = handedToSecurity;
        this.finderNotes = finderNotes;
    }

    /** Overrides the abstract method: a found report is always FOUND. */
    @Override
    public ItemType getItemType() {
        return ItemType.FOUND;
    }

    @Override
    public String getSpecificDetails() {
        return "Stored at    : " + (storageLocation == null ? "-" : storageLocation)
                + "\nWith security: " + (handedToSecurity ? "Yes" : "No")
                + "\nFinder notes : " + (finderNotes == null || finderNotes.isEmpty()
                        ? "-" : finderNotes);
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public boolean isHandedToSecurity() {
        return handedToSecurity;
    }

    public void setHandedToSecurity(boolean handedToSecurity) {
        this.handedToSecurity = handedToSecurity;
    }

    public String getFinderNotes() {
        return finderNotes;
    }

    public void setFinderNotes(String finderNotes) {
        this.finderNotes = finderNotes;
    }
}
