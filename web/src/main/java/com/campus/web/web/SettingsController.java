package com.campus.web.web;

import com.campus.web.model.User;
import com.campus.web.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SettingsController {

    private final UserService userService;

    public SettingsController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/settings")
    public String settings(Model model,
                           Authentication authentication) {
        model.addAttribute("user", userService.currentUser(authentication));
        return "settings";
    }

    @PostMapping("/settings/profile")
    public String updateProfile(@RequestParam String fullName,
                                @RequestParam String phone,
                                @RequestParam(required = false) String gender,
                                Authentication authentication,
                                RedirectAttributes redirect) {
        try {
            userService.updateProfile(userService.currentUser(authentication),
                    fullName, phone, gender);
            redirect.addFlashAttribute("message", "Profile updated.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/settings/password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication authentication,
                                 RedirectAttributes redirect) {
        try {
            userService.changePassword(userService.currentUser(authentication),
                    currentPassword, newPassword, confirmPassword);
            redirect.addFlashAttribute("message", "Password changed.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/settings";
    }
}
