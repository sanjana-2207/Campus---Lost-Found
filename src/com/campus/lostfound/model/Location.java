package com.campus.lostfound.model;

/** Lookup value mapped from the {@code locations} table. */
public class Location {

    private int locationId;
    private String name;
    private String building;

    public Location() {
    }

    public Location(int locationId, String name, String building) {
        this.locationId = locationId;
        this.name = name;
        this.building = building;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    @Override
    public String toString() {
        return name;
    }
}
