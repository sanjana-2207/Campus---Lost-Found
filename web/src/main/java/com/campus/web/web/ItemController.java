package com.campus.web.web;

import com.campus.web.model.Item;
import com.campus.web.model.ItemStatus;
import com.campus.web.model.ItemType;
import com.campus.web.model.User;
import com.campus.web.service.ClaimService;
import com.campus.web.service.ItemService;
import com.campus.web.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Controller
public class ItemController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");

    private final ItemService itemService;
    private final ClaimService claimService;
    private final UserService userService;

    public ItemController(ItemService itemService, ClaimService claimService,
                          UserService userService) {
        this.itemService = itemService;
        this.claimService = claimService;
        this.userService = userService;
    }

    @GetMapping("/found-items")
    public String foundItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String dateMode,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) String timeMode,
            Model model) {

        LocalDate parsedDate = parseDate(date, model);
        LocalTime parsedTime = parseTime(time, model);

        List<Item> results = itemService.searchFound(
                keyword, categoryId, locationId, parsedDate, dateMode,
                parsedTime, timeMode);

        model.addAttribute("items", results);
        model.addAttribute("categories", itemService.categories());
        model.addAttribute("locations", itemService.locations());
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("locationId", locationId);
        model.addAttribute("date", date == null ? "" : date);
        model.addAttribute("dateMode", dateMode == null ? "any" : dateMode);
        model.addAttribute("time", time == null ? "" : time);
        model.addAttribute("timeMode", timeMode == null ? "any" : timeMode);
        return "found-items";
    }

    @GetMapping("/found-items/{id}")
    public String itemDetail(@PathVariable Long id,
                             @ModelAttribute("currentUser") User currentUser,
                             Model model) {
        Item item = itemService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found."));
        boolean ownItem = currentUser != null && item.getReporter() != null
                && item.getReporter().getId().equals(currentUser.getId());
        boolean claimable = item.getType() == ItemType.FOUND
                && !ownItem
                && (item.getStatus() == ItemStatus.OPEN
                    || item.getStatus() == ItemStatus.MATCHED);
        model.addAttribute("item", item);
        model.addAttribute("claimable", claimable);
        model.addAttribute("ownItem", ownItem);
        return "item-detail";
    }

    @PostMapping("/found-items/{id}/claim")
    public String claim(@PathVariable Long id,
                        @RequestParam String proof,
                        Authentication authentication,
                        RedirectAttributes redirect) {
        User user = userService.currentUser(authentication);
        try {
            Item item = itemService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Item not found."));
            claimService.submitClaim(item, user, proof);
            redirect.addFlashAttribute("message",
                    "Your claim was submitted. Staff will verify it.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/found-items/" + id;
    }

    @GetMapping("/report-found")
    public String reportForm(Model model) {
        model.addAttribute("categories", itemService.categories());
        model.addAttribute("locations", itemService.locations());
        model.addAttribute("today", LocalDate.now().format(DAY));
        return "report-found";
    }

    @PostMapping("/report-found")
    public String reportFound(@RequestParam String title,
                              @RequestParam String description,
                              @RequestParam Long categoryId,
                              @RequestParam Long locationId,
                              @RequestParam String eventDate,
                              @RequestParam String storageLocation,
                              @RequestParam(required = false) Boolean handedToSecurity,
                              @RequestParam(required = false) String finderNotes,
                              @RequestParam(required = false) MultipartFile photo,
                              Authentication authentication,
                              Model model,
                              RedirectAttributes redirect) {
        User user = userService.currentUser(authentication);
        try {
            LocalDate when;
            try {
                when = LocalDate.parse(eventDate, DAY);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Enter the date as dd-MM-yyyy.");
            }
            Item item = itemService.reportFound(title, description, categoryId,
                    locationId, when, storageLocation,
                    Boolean.TRUE.equals(handedToSecurity), finderNotes, photo, user);
            redirect.addFlashAttribute("message",
                    "Report #" + item.getId() + " saved. Owners can now find it in Found Items.");
            return "redirect:/found-items/" + item.getId();
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("categories", itemService.categories());
            model.addAttribute("locations", itemService.locations());
            model.addAttribute("today", LocalDate.now().format(DAY));
            model.addAttribute("title", title);
            model.addAttribute("description", description);
            model.addAttribute("categoryId", categoryId);
            model.addAttribute("locationId", locationId);
            model.addAttribute("eventDate", eventDate);
            model.addAttribute("storageLocation", storageLocation);
            model.addAttribute("finderNotes", finderNotes);
            model.addAttribute("handedToSecurity", handedToSecurity);
            return "report-found";
        }
    }

    @GetMapping("/items/{id}/photo")
    public ResponseEntity<byte[]> photo(@PathVariable Long id) {
        Item item = itemService.findById(id).orElse(null);
        if (item == null || !item.isHasPhoto()) {
            return ResponseEntity.notFound().build();
        }
        MediaType type = item.getPhotoContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(item.getPhotoContentType());
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(type)
                .body(item.getPhoto());
    }

    private LocalDate parseDate(String value, Model model) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), DAY);
        } catch (DateTimeParseException ex) {
            model.addAttribute("error", "Enter the date as dd-MM-yyyy.");
            return null;
        }
    }

    private LocalTime parseTime(String value, Model model) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(value.trim(), CLOCK);
        } catch (DateTimeParseException ex) {
            model.addAttribute("error", "Enter the time as HH:mm.");
            return null;
        }
    }
}
