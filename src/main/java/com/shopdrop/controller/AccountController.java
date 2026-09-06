package com.shopdrop.controller;

import com.shopdrop.model.User;
import com.shopdrop.repository.UserRepository;
import com.shopdrop.service.OrderService;
import com.shopdrop.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final OrderService orderService;

    public AccountController(UserRepository userRepository, UserService userService, OrderService orderService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.orderService = orderService;
    }

    @GetMapping
    public String view(Authentication authentication, Model model) {
        User user = currentUser(authentication);
        model.addAttribute("user", user);
        model.addAttribute("orders", orderService.findByUser(user));
        return "account";
    }

    @PostMapping("/membership")
    public String updateMembership(Authentication authentication,
                                    @RequestParam(defaultValue = "false") boolean member) {
        User user = currentUser(authentication);
        userService.setMembership(user, member);
        return "redirect:/account";
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + authentication.getName()));
    }
}
