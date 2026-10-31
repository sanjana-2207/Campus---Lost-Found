package com.campus.lostfound.model;

import java.time.LocalDate;

/**
 * Search criteria collected from the Search screen.
 *
 * <p>An empty/null field means "no filter". The DAO turns this object into a
 * single parameterised SQL statement, which keeps the user interface free of
 * SQL knowledge and prevents SQL injection.</p>
 *
 * <p>The fluent setters ({@code return this}) make call sites readable:</p>
 * <pre>
 * ItemFilter filter = new ItemFilter()
 *         .setCategoryId(1)
 *         .setLocationId(3)
 *         .setKeyword("charger");
 * </pre>
 */
public class ItemFilter {

    private ItemType itemType;
    private ItemStatus status;
    private Integer categoryId;
    private Integer locationId;
    private Integer reporterId;
    private String keyword;
    private LocalDate fromDate;
    private LocalDate toDate;

    public ItemType getItemType() {
        return itemType;
    }

    public ItemFilter setItemType(ItemType itemType) {
        this.itemType = itemType;
        return this;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public ItemFilter setStatus(ItemStatus status) {
        this.status = status;
        return this;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public ItemFilter setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public ItemFilter setLocationId(Integer locationId) {
        this.locationId = locationId;
        return this;
    }

    public Integer getReporterId() {
        return reporterId;
    }

    public ItemFilter setReporterId(Integer reporterId) {
        this.reporterId = reporterId;
        return this;
    }

    public String getKeyword() {
        return keyword;
    }

    public ItemFilter setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public ItemFilter setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
        return this;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public ItemFilter setToDate(LocalDate toDate) {
        this.toDate = toDate;
        return this;
    }
}
