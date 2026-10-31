package com.campus.lostfound.model;

import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * Super-class of the item hierarchy.
 *
 * <p><b>Abstraction:</b> this class describes what every reported item has in
 * common and refuses to exist on its own ({@code abstract}). You can never write
 * {@code new Item()} - you must create a concrete {@link LostItem} or
 * {@link FoundItem}.</p>
 *
 * <p><b>Inheritance:</b> the common state (title, category, location, date,
 * reporter, status) lives here exactly once, so both sub-classes reuse it.</p>
 *
 * <p><b>Polymorphism:</b> the user interface stores items in a
 * {@code List<Item>} and calls {@link #getItemType()} / {@link #getSpecificDetails()}
 * without knowing which sub-class it is holding.</p>
 *
 * <p><b>Encapsulation:</b> all fields are private with public accessors.</p>
 */
public abstract class Item {

    private int itemId;
    private String title;
    private String description;
    private Category category;
    private Location location;
    private LocalDate eventDate;
    private User reportedBy;
    private ItemStatus status = ItemStatus.OPEN;
    private Timestamp createdAt;

    protected Item() {
        // Required by the DAO while mapping a row from the database.
    }

    protected Item(String title, String description, Category category,
                   Location location, LocalDate eventDate, User reportedBy) {
        setTitle(title);
        setDescription(description);
        setCategory(category);
        setLocation(location);
        setEventDate(eventDate);
        setReportedBy(reportedBy);
    }

    /**
     * Returns the concrete type. Implemented by every sub-class, which is the
     * classic use of an abstract method.
     */
    public abstract ItemType getItemType();

    /**
     * Returns the sub-class specific fields as one readable line.
     * Used by the details dialog, so the UI does not need {@code instanceof}.
     */
    public abstract String getSpecificDetails();

    /** Shared formatting used by tables and dialogs. */
    public String describe() {
        return "#" + itemId
                + "  " + getItemType().getLabel().toUpperCase()
                + "  |  " + safe(title)
                + "  |  " + (category == null ? "-" : category.getName())
                + "  |  " + (location == null ? "-" : location.getName())
                + "  |  " + (eventDate == null ? "-" : eventDate)
                + "  |  " + status.getLabel();
    }

    private static String safe(String value) {
        return value == null ? "-" : value;
    }

    // ------------------------------------------------------------------
    //  Encapsulated accessors
    // ------------------------------------------------------------------

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public User getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(User reportedBy) {
        this.reportedBy = reportedBy;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status == null ? ItemStatus.OPEN : status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /** Null-safe helper for table models and CSV export. */
    public String getCategoryName() {
        return category == null ? "" : category.getName();
    }

    /** Null-safe helper for table models and CSV export. */
    public String getLocationName() {
        return location == null ? "" : location.getName();
    }

    /** Null-safe helper for table models and CSV export. */
    public String getReporterName() {
        return reportedBy == null ? "" : reportedBy.getFullName();
    }

    @Override
    public String toString() {
        return "#" + itemId + " " + title;
    }
}
