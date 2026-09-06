package com.shopdrop.controller;

import com.shopdrop.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String name,
                            @RequestParam String email,
                            @RequestParam String password,
                            @RequestParam String confirmPassword,
                            Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            return "register";
        }

        try {
            userService.register(name, email, password);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            return "register";
        }

        return "redirect:/login?registered";
    }
}
