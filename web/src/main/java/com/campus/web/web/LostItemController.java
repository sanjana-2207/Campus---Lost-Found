package com.campus.web.web;

import com.campus.web.model.Item;
import com.campus.web.model.ItemType;
import com.campus.web.model.User;
import com.campus.web.service.ClaimService;
import com.campus.web.service.ItemService;
import com.campus.web.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Controller
public class LostItemController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final ItemService itemService;
    private final ClaimService claimService;
    private final UserService userService;

    public LostItemController(ItemService itemService, ClaimService claimService,
                              UserService userService) {
        this.itemService = itemService;
        this.claimService = claimService;
        this.userService = userService;
    }

    @GetMapping("/lost-items")
    public String lostItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String dateMode,
            Model model) {

        LocalDate parsedDate = parseDate(date, model);

        List<Item> results = itemService.searchLost(
                keyword, categoryId, locationId, parsedDate, dateMode);

        model.addAttribute("items", results);
        model.addAttribute("categories", itemService.categories());
        model.addAttribute("locations", itemService.locations());
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("locationId", locationId);
        model.addAttribute("date", date == null ? "" : date);
        model.addAttribute("dateMode", dateMode == null ? "any" : dateMode);
        return "lost-items";
    }

    @GetMapping("/lost-items/{id}")
    public String lostItemDetail(@PathVariable Long id,
                                 Authentication authentication,
                                 Model model) {
        Item item = itemService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found."));
        if (item.getType() != ItemType.LOST) {
            return "redirect:/found-items/" + id;
        }
        User user = userService.currentUser(authentication);
        boolean ownItem = user != null && item.getReporter() != null
                && item.getReporter().getId().equals(user.getId());
        model.addAttribute("item", item);
        model.addAttribute("ownItem", ownItem);
        return "lost-item-detail";
    }

    @PostMapping("/lost-items/{id}/claim")
    public String claimLostItem(@PathVariable Long id,
                                @RequestParam String proof,
                                Authentication authentication,
                                RedirectAttributes redirect) {
        User user = userService.currentUser(authentication);
        Item lost = itemService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found."));
        if (lost.getType() != ItemType.LOST) {
            return "redirect:/found-items/" + id;
        }

        Optional<Item> match = itemService.findClaimableMatchForLost(lost, user);
        if (match.isEmpty()) {
            redirect.addFlashAttribute("error",
                    "No matching found item is available to claim yet. "
                            + "Please check Found Items or contact the security office.");
            return "redirect:/lost-items/" + id;
        }

        try {
            claimService.submitClaim(match.get(), user, proof);
            redirect.addFlashAttribute("message",
                    "Your claim was submitted for \"" + match.get().getTitle()
                            + "\". Staff will verify it.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/lost-items/" + id;
    }

    @GetMapping("/report-lost")
    public String reportForm(Model model) {
        model.addAttribute("categories", itemService.categories());
        model.addAttribute("locations", itemService.locations());
        model.addAttribute("today", LocalDate.now().format(DAY));
        return "report-lost";
    }

    @PostMapping("/report-lost")
    public String reportLost(@RequestParam String title,
                             @RequestParam String description,
                             @RequestParam Long categoryId,
                             @RequestParam Long locationId,
                             @RequestParam String eventDate,
                             @RequestParam(required = false) String contactInfo,
                             @RequestParam(required = false) String reward,
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
            Item item = itemService.reportLost(title, description, categoryId,
                    locationId, when, contactInfo, reward, user);
            redirect.addFlashAttribute("message",
                    "Lost report #" + item.getId()
                            + " saved. Finders can now see it in Lost Items.");
            return "redirect:/lost-items/" + item.getId();
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
            model.addAttribute("contactInfo", contactInfo);
            model.addAttribute("reward", reward);
            return "report-lost";
        }
    }

    @GetMapping("/my-reports")
    public String myReports(Authentication authentication, Model model) {
        User user = userService.currentUser(authentication);
        model.addAttribute("reports", itemService.reportsBy(user.getId()));
        return "my-reports";
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
}
