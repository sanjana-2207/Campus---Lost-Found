package com.campus.lostfound.service;

import com.campus.lostfound.dao.ItemDAO;
import com.campus.lostfound.dao.JdbcItemDAO;
import com.campus.lostfound.dao.JdbcLookupDAO;
import com.campus.lostfound.dao.LookupDAO;
import com.campus.lostfound.exception.ValidationException;
import com.campus.lostfound.model.Category;
import com.campus.lostfound.model.FoundItem;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.ItemFilter;
import com.campus.lostfound.model.ItemStatus;
import com.campus.lostfound.model.Location;
import com.campus.lostfound.model.LostItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Business rules for reporting and searching items.
 *
 * <p>All input validation lives here, not in the Swing form, so the same rules
 * apply whether an item is created from the UI, a test or a future REST API.</p>
 */
public class ItemService {

    private final ItemDAO itemDAO;
    private final LookupDAO lookupDAO;

    public ItemService() {
        this(new JdbcItemDAO(), new JdbcLookupDAO());
    }

    public ItemService(ItemDAO itemDAO, LookupDAO lookupDAO) {
        this.itemDAO = itemDAO;
        this.lookupDAO = lookupDAO;
    }

    /** Validates and stores a new report, returning the generated id. */
    public int reportItem(Item item) {
        validate(item);
        return itemDAO.insert(item);
    }

    public List<Item> search(ItemFilter filter) {
        return itemDAO.search(filter);
    }

    public Item findById(int itemId) {
        return itemDAO.findById(itemId);
    }

    public List<Item> recent(int limit) {
        return itemDAO.findRecent(limit);
    }

    public List<Item> reportsBy(int userId) {
        return itemDAO.findByReporter(userId);
    }

    public List<Category> categories() {
        return lookupDAO.findAllCategories();
    }

    public List<Location> locations() {
        return lookupDAO.findAllLocations();
    }

    public boolean changeStatus(int itemId, ItemStatus status) {
        return itemDAO.updateStatus(itemId, status);
    }

    // ------------------------------------------------------------------
    //  Validation
    // ------------------------------------------------------------------

    private void validate(Item item) {
        if (item == null) {
            throw new ValidationException("Nothing to save.");
        }
        if (isBlank(item.getTitle()) || item.getTitle().trim().length() < 4) {
            throw new ValidationException("Give the item a short title (at least 4 characters).");
        }
        if (isBlank(item.getDescription()) || item.getDescription().trim().length() < 10) {
            throw new ValidationException(
                    "Describe the item in at least 10 characters so it can be identified.");
        }
        if (item.getCategory() == null) {
            throw new ValidationException("Select a category.");
        }
        if (item.getLocation() == null) {
            throw new ValidationException("Select the location where the item was lost or found.");
        }
        if (item.getEventDate() == null) {
            throw new ValidationException("Choose the date of the incident.");
        }
        if (item.getEventDate().isAfter(LocalDate.now())) {
            throw new ValidationException("The date of the incident cannot be in the future.");
        }
        if (item.getReportedBy() == null) {
            throw new ValidationException("You must be signed in to report an item.");
        }

        if (item instanceof LostItem) {
            LostItem lost = (LostItem) item;
            if (isBlank(lost.getContact())) {
                throw new ValidationException("Add a contact number or e-mail so finders can reach you.");
            }
            if (lost.getRewardOffered() != null
                    && lost.getRewardOffered().compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("The reward amount cannot be negative.");
            }
        } else if (item instanceof FoundItem) {
            FoundItem found = (FoundItem) item;
            if (isBlank(found.getStorageLocation())) {
                throw new ValidationException(
                        "Tell the owner where the item is kept (for example: Security office).");
            }
        }

        // Normalise the text once, so the database never stores stray spaces.
        item.setTitle(item.getTitle().trim());
        item.setDescription(item.getDescription().trim());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
