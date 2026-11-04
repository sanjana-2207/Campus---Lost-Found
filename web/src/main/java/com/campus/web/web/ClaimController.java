package com.campus.web.web;

import com.campus.web.model.User;
import com.campus.web.service.ClaimService;
import com.campus.web.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ClaimController {

    private final ClaimService claimService;
    private final UserService userService;

    public ClaimController(ClaimService claimService, UserService userService) {
        this.claimService = claimService;
        this.userService = userService;
    }

    @GetMapping("/claims")
    public String claims(Authentication authentication,
                         @RequestParam(required = false, defaultValue = "mine") String tab,
                         Model model) {
        User user = userService.currentUser(authentication);
        model.addAttribute("myClaims", claimService.claimsOf(user.getId()));
        model.addAttribute("reportedClaims", claimService.claimsForReporter(user.getId()));
        if (user.isVerifier()) {
            model.addAttribute("allClaims", claimService.allClaims());
        }
        model.addAttribute("tab", tab);
        return "claims";
    }

    @PostMapping("/claims/{id}/approve")
    public String approve(@PathVariable Long id,
                          @RequestParam(required = false) String remarks,
                          Authentication authentication,
                          RedirectAttributes redirect) {
        try {
            claimService.approve(id, userService.currentUser(authentication), remarks);
            redirect.addFlashAttribute("message", "Claim approved and item reserved.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/claims?tab=all";
    }

    @PostMapping("/claims/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam(required = false) String remarks,
                         Authentication authentication,
                         RedirectAttributes redirect) {
        try {
            claimService.reject(id, userService.currentUser(authentication), remarks);
            redirect.addFlashAttribute("message", "Claim rejected.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/claims?tab=all";
    }

    @PostMapping("/claims/{id}/confirm")
    public String confirm(@PathVariable Long id,
                          Authentication authentication,
                          RedirectAttributes redirect) {
        try {
            claimService.confirmHandover(id, userService.currentUser(authentication));
            redirect.addFlashAttribute("message",
                    "Handover confirmed. The item is now archived for reports.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/claims?tab=reported";
    }
}
