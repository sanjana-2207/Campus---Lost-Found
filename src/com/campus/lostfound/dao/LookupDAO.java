package com.campus.lostfound.dao;

import com.campus.lostfound.model.Category;
import com.campus.lostfound.model.Location;

import java.util.List;

/** Read-only access to the two lookup tables used by combo boxes. */
public interface LookupDAO {

    List<Category> findAllCategories();

    List<Location> findAllLocations();
}
