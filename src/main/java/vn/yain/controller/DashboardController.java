package vn.yain.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import vn.yain.service.UserService;

@Controller
public class DashboardController {

    @Autowired(required = false)
    private UserService userService;

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

    @GetMapping("/customer/profile")
    public String customerProfile() {
        return "customer/profile";
    }

    // MANAGER ENDPOINTS
    @GetMapping("/manager/courts")
    public String managerCourts() {
        return "manager/courts";
    }
}
