package com.campus.lostfound.model;

import java.time.LocalDate;

/**
 * Read-only result produced by the matching engine ({@code vw_potential_matches}).
 *
 * <p>Deliberately a flat data-transfer object: it is a report, not an entity,
 * so it has no setter-driven life cycle in the database.</p>
 */
public class MatchResult {

    private int lostItemId;
    private String lostTitle;
    private int foundItemId;
    private String foundTitle;
    private String categoryName;
    private String locationName;
    private LocalDate lostDate;
    private LocalDate foundDate;
    private int daysApart;
    private int score;

    public int getLostItemId() {
        return lostItemId;
    }

    public void setLostItemId(int lostItemId) {
        this.lostItemId = lostItemId;
    }

    public String getLostTitle() {
        return lostTitle;
    }

    public void setLostTitle(String lostTitle) {
        this.lostTitle = lostTitle;
    }

    public int getFoundItemId() {
        return foundItemId;
    }

    public void setFoundItemId(int foundItemId) {
        this.foundItemId = foundItemId;
    }

    public String getFoundTitle() {
        return foundTitle;
    }

    public void setFoundTitle(String foundTitle) {
        this.foundTitle = foundTitle;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public LocalDate getLostDate() {
        return lostDate;
    }

    public void setLostDate(LocalDate lostDate) {
        this.lostDate = lostDate;
    }

    public LocalDate getFoundDate() {
        return foundDate;
    }

    public void setFoundDate(LocalDate foundDate) {
        this.foundDate = foundDate;
    }

    public int getDaysApart() {
        return daysApart;
    }

    public void setDaysApart(int daysApart) {
        this.daysApart = daysApart;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    /** Human readable confidence used in the Matches screen. */
    public String getConfidence() {
        if (score >= 90) {
            return "Very High";
        }
        if (score >= 80) {
            return "High";
        }
        if (score >= 70) {
            return "Medium";
        }
        return "Low";
    }
}
