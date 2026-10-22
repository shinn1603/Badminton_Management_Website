package vn.yain.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.yain.repository.BranchRepository;
import vn.yain.repository.UserRepository;
import vn.yain.service.UserService;

@Controller
public class DashboardController {

    @Autowired(required = false)
    private UserService userService;

    @Autowired(required = false)
    private BranchRepository branchRepository;

    @Autowired(required = false)
    private UserRepository userRepository;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // CUSTOMER PORTAL ENDPOINTS
    @GetMapping("/customer/home")
    public String customerHome() {
        return "customer/home";
    }

    @GetMapping({ "/customer/auth", "/customer/login" })
    public String customerAuth() {
        return "customer/auth";
    }

    @GetMapping("/customer/booking")
    public String customerBooking() {
        return "customer/booking";
    }

    @GetMapping("/customer/payment")
    public String customerPayment() {
        return "customer/payment";
    }

    @GetMapping("/customer/history")
    public String customerHistory() {
        return "customer/history";
    }

    @GetMapping("/customer/profile")
    public String customerProfile() {
        return "customer/profile";
    }

    @GetMapping({ "/customer/policy", "/policy" })
    public String customerPolicy() {
        return "customer/policy";
    }

    @GetMapping({ "/booking/grid", "/pos/grid" })
    public String bookingGrid() {
        return "pos/grid";
    }

    // MANAGER ENDPOINTS
    @GetMapping({ "/manager/bookings", "/pos/bookings" })
    public String managerBookings() {
        return "manager/bookings";
    }

    @GetMapping("/manager/courts")
    public String managerCourts() {
        return "manager/courts";
    }

    // ADMIN ENDPOINTS (DAY 5 SCOPE)
    @GetMapping("/admin/users")
    public String adminUsers(Model model) {
        if (userService != null) {
            model.addAttribute("users", userService.getAllUsers());
        }
        return "admin/users";
    }

    @GetMapping("/admin/permissions")
    public String adminPermissions(Model model) {
        if (userService != null) {
            model.addAttribute("users", userService.getAllUsers());
        }
        if (branchRepository != null) {
            model.addAttribute("branches", branchRepository.findAll());
        }
        return "admin/permissions";
    }

    // ADMIN ENDPOINTS (DAY 6 SCOPE)
    @GetMapping("/admin/settings")
    public String adminSettings() {
        return "admin/settings";
    }

    @GetMapping("/admin/logs")
    public String adminLogs() {
        return "admin/logs";
    }
}
