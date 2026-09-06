package com.shopdrop.controller;

import com.shopdrop.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/orders")
public class AdminController {

    private final OrderService orderService;

    public AdminController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("orders", orderService.findAll());
        return "admin-orders";
    }

    @PostMapping("/{id}/advance")
    public String advance(@PathVariable Long id) {
        orderService.advance(id);
        return "redirect:/admin/orders";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return "redirect:/admin/orders";
    }
}
