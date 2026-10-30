package com.campus.lostfound.model;

/** Lookup value mapped from the {@code categories} table. */
public class Category {

    private int categoryId;
    private String name;
    private String description;

    public Category() {
    }

    public Category(int categoryId, String name, String description) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /** JComboBox renders this text, so only the name is shown. */
    @Override
    public String toString() {
        return name;
    }
}
