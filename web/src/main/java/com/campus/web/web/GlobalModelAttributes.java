package com.campus.web.web;

import com.campus.web.model.User;
import com.campus.web.service.ClaimService;
import com.campus.web.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final UserService userService;
    private final ClaimService claimService;

    public GlobalModelAttributes(UserService userService, ClaimService claimService) {
        this.userService = userService;
        this.claimService = claimService;
    }

    @ModelAttribute("currentUser")
    public User currentUser(Authentication authentication) {
        return userService.currentUser(authentication);
    }

    @ModelAttribute("isVerifier")
    public boolean isVerifier(Authentication authentication) {
        User user = userService.currentUser(authentication);
        return user != null && user.isVerifier();
    }

    @ModelAttribute("notificationCount")
    public long notificationCount(Authentication authentication) {
        User user = userService.currentUser(authentication);
        return claimService.actionableCount(user);
    }
}
