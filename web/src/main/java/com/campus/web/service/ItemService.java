package com.campus.web.service;

import com.campus.web.model.Category;
import com.campus.web.model.Item;
import com.campus.web.model.ItemStatus;
import com.campus.web.model.ItemType;
import com.campus.web.model.Location;
import com.campus.web.model.User;
import com.campus.web.repository.CategoryRepository;
import com.campus.web.repository.ItemRepository;
import com.campus.web.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class ItemService {

    private static final long MAX_PHOTO_BYTES = 8L * 1024L * 1024L;

    private final ItemRepository items;
    private final CategoryRepository categories;
    private final LocationRepository locations;

    public ItemService(ItemRepository items, CategoryRepository categories,
                       LocationRepository locations) {
        this.items = items;
        this.categories = categories;
        this.locations = locations;
    }

    public List<Category> categories() {
        return categories.findAllByOrderByNameAsc();
    }

    public List<Location> locations() {
        return locations.findAllByOrderByNameAsc();
    }

    public Optional<Item> findById(Long id) {
        return items.findById(id);
    }

    public List<Item> reportsBy(Long userId) {
        return items.findByReporterIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Found items that are still claimable. Filtering is done in Java because
     * the demo dataset is small and it keeps the query portable to H2.
     */
    public List<Item> searchFound(String keyword, Long categoryId, Long locationId,
                                  LocalDate date, String dateMode,
                                  LocalTime time, String timeMode) {
        List<Item> found = items.findByTypeAndStatusInOrderByCreatedAtDesc(
                ItemType.FOUND, List.of(ItemStatus.OPEN, ItemStatus.MATCHED));

        String search = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        String dayMode = dateMode == null ? "any" : dateMode;
        String clockMode = timeMode == null ? "any" : timeMode;

        List<Item> result = new ArrayList<>();
        for (Item item : found) {
            if (!search.isEmpty()
                    && !contains(item.getTitle(), search)
                    && !contains(item.getDescription(), search)) {
                continue;
            }
            if (categoryId != null
                    && (item.getCategory() == null
                        || !categoryId.equals(item.getCategory().getId()))) {
                continue;
            }
            if (locationId != null
                    && (item.getLocation() == null
                        || !locationId.equals(item.getLocation().getId()))) {
                continue;
            }
            if (date != null && !"any".equals(dayMode)) {
                LocalDate event = item.getEventDate();
                if (event == null) {
                    continue;
                }
                if ("on".equals(dayMode) && !event.equals(date)) {
                    continue;
                }
                if ("before".equals(dayMode) && event.isAfter(date)) {
                    continue;
                }
                if ("after".equals(dayMode) && event.isBefore(date)) {
                    continue;
                }
            }
            if (time != null && !"any".equals(clockMode)) {
                LocalTime reported = item.getCreatedAt() == null
                        ? null : item.getCreatedAt().toLocalTime();
                if (reported == null) {
                    continue;
                }
                if ("after".equals(clockMode) && reported.isBefore(time)) {
                    continue;
                }
                if ("before".equals(clockMode) && reported.isAfter(time)) {
                    continue;
                }
            }
            result.add(item);
        }
        return result;
    }

    /**
     * Lost reports are visible to everyone so finders can recognise an item
     * and contact its owner. All statuses are included so the history stays
     * intact after an item is recovered.
     */
    public List<Item> searchLost(String keyword, Long categoryId, Long locationId,
                                 LocalDate date, String dateMode) {
        List<Item> lost = items.findByTypeOrderByCreatedAtDesc(ItemType.LOST);

        String search = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        String dayMode = dateMode == null ? "any" : dateMode;

        List<Item> result = new ArrayList<>();
        for (Item item : lost) {
            if (!search.isEmpty()
                    && !contains(item.getTitle(), search)
                    && !contains(item.getDescription(), search)) {
                continue;
            }
            if (categoryId != null
                    && (item.getCategory() == null
                        || !categoryId.equals(item.getCategory().getId()))) {
                continue;
            }
            if (locationId != null
                    && (item.getLocation() == null
                        || !locationId.equals(item.getLocation().getId()))) {
                continue;
            }
            if (date != null && !"any".equals(dayMode)) {
                LocalDate event = item.getEventDate();
                if (event == null) {
                    continue;
                }
                if ("on".equals(dayMode) && !event.equals(date)) {
                    continue;
                }
                if ("before".equals(dayMode) && event.isAfter(date)) {
                    continue;
                }
                if ("after".equals(dayMode) && event.isBefore(date)) {
                    continue;
                }
            }
            result.add(item);
        }
        return result;
    }

    /**
     * Best claimable found item for a lost report. Owners searching from a lost
     * report are pointed at the found item that matches it, so the proof of
     * ownership is filed against the actual item waiting at the security desk.
     */
    public Optional<Item> findClaimableMatchForLost(Item lost, User claimant) {
        List<Item> candidates = items.findByTypeAndStatusInOrderByCreatedAtDesc(
                ItemType.FOUND, List.of(ItemStatus.OPEN, ItemStatus.MATCHED));

        Item best = null;
        long bestScore = Long.MAX_VALUE;
        for (Item candidate : candidates) {
            if (lost.getCategory() == null || candidate.getCategory() == null
                    || !lost.getCategory().getId().equals(candidate.getCategory().getId())) {
                continue;
            }
            if (claimant != null && candidate.getReporter() != null
                    && candidate.getReporter().getId().equals(claimant.getId())) {
                continue;
            }
            long score = 0;
            if (lost.getLocation() != null && candidate.getLocation() != null
                    && lost.getLocation().getId().equals(candidate.getLocation().getId())) {
                score -= 1000;
            }
            if (lost.getEventDate() != null && candidate.getEventDate() != null) {
                score += Math.abs(ChronoUnit.DAYS.between(
                        lost.getEventDate(), candidate.getEventDate()));
            }
            if (score < bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return Optional.ofNullable(best);
    }

    @Transactional
    public Item reportFound(String title, String description, Long categoryId,
                            Long locationId, LocalDate eventDate,
                            String storageLocation, boolean handedToSecurity,
                            String finderNotes, MultipartFile photo, User reporter) {
        String cleanTitle = clean(title);
        String cleanDescription = clean(description);
        String storage = clean(storageLocation);

        if (cleanTitle.length() < 4) {
            throw new IllegalArgumentException("Give the item a short title (at least 4 characters).");
        }
        if (cleanDescription.length() < 10) {
            throw new IllegalArgumentException("Describe the item in at least 10 characters.");
        }
        if (storage.isEmpty()) {
            throw new IllegalArgumentException("Tell the owner where the item is kept.");
        }
        if (eventDate == null || eventDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Choose a valid date of the incident.");
        }

        Category category = categories.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Select a category."));
        Location location = locations.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Select a location."));

        Item item = new Item();
        item.setType(ItemType.FOUND);
        item.setTitle(cleanTitle);
        item.setDescription(cleanDescription);
        item.setCategory(category);
        item.setLocation(location);
        item.setEventDate(eventDate);
        item.setReporter(reporter);
        item.setStatus(ItemStatus.OPEN);
        item.setCreatedAt(java.time.LocalDateTime.now());
        item.setStorageLocation(storage);
        item.setHandedToSecurity(handedToSecurity);
        item.setFinderNotes(clean(finderNotes));

        if (photo != null && !photo.isEmpty()) {
            String contentType = photo.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Choose a valid image file.");
            }
            if (photo.getSize() > MAX_PHOTO_BYTES) {
                throw new IllegalArgumentException("The photo must be 8 MB or smaller.");
            }
            try {
                item.setPhoto(photo.getBytes());
                item.setPhotoContentType(contentType);
            } catch (IOException ex) {
                throw new IllegalArgumentException("The photo could not be read.");
            }
        }

        return items.save(item);
    }

    @Transactional
    public Item reportLost(String title, String description, Long categoryId,
                           Long locationId, LocalDate eventDate,
                           String contactInfo, String reward, User reporter) {
        String cleanTitle = clean(title);
        String cleanDescription = clean(description);
        String cleanContact = clean(contactInfo);
        String cleanReward = clean(reward);

        if (cleanTitle.length() < 4) {
            throw new IllegalArgumentException("Give the item a short title (at least 4 characters).");
        }
        if (cleanDescription.length() < 10) {
            throw new IllegalArgumentException("Describe the item in at least 10 characters.");
        }
        if (cleanContact.length() > 120) {
            throw new IllegalArgumentException("Contact details must be 120 characters or fewer.");
        }
        if (cleanReward.length() > 80) {
            throw new IllegalArgumentException("The reward note must be 80 characters or fewer.");
        }
        if (eventDate == null || eventDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Choose a valid date of the incident.");
        }

        Category category = categories.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Select a category."));
        Location location = locations.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Select a location."));

        Item item = new Item();
        item.setType(ItemType.LOST);
        item.setTitle(cleanTitle);
        item.setDescription(cleanDescription);
        item.setCategory(category);
        item.setLocation(location);
        item.setEventDate(eventDate);
        item.setReporter(reporter);
        item.setStatus(ItemStatus.OPEN);
        item.setCreatedAt(java.time.LocalDateTime.now());
        item.setContactInfo(cleanContact);
        item.setReward(cleanReward);
        return items.save(item);
    }

    private boolean contains(String value, String search) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(search);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
