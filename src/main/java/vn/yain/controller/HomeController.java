package vn.yain.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({ "/", "/customer" })
    public String index() {
        return "redirect:/customer/home";
    }

    @GetMapping("/customer/home")
    public String home() {
        return "customer/home";
    }
}
