package com.campus.web.web;

import com.campus.web.model.User;
import com.campus.web.service.NotificationService;
import com.campus.web.service.ReportService;
import com.campus.web.service.ReportSummary;
import com.campus.web.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final ReportService reportService;
    private final NotificationService notificationService;
    private final UserService userService;

    public DashboardController(ReportService reportService,
                               NotificationService notificationService,
                               UserService userService) {
        this.reportService = reportService;
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        ReportSummary summary = reportService.buildSummary();
        model.addAttribute("summary", summary);
        return "dashboard";
    }

    @GetMapping("/notifications")
    public String notifications(Authentication authentication, Model model) {
        User user = userService.currentUser(authentication);
        model.addAttribute("notifications", notificationService.buildFor(user));
        return "notifications";
    }
}
