package com.shopdrop.controller;

import com.shopdrop.model.Order;
import com.shopdrop.service.OrderService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Authentication authentication, Model model) {
        Order order = orderService.findById(id);
        requireOwnerOrAdmin(order, authentication);
        model.addAttribute("order", order);
        return "order-status";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, Authentication authentication) {
        Order order = orderService.findById(id);
        requireOwnerOrAdmin(order, authentication);
        orderService.cancel(id);
        return "redirect:/orders/" + id;
    }

    /** A regular user may only see/cancel their own orders; an admin may see/cancel any order. */
    private void requireOwnerOrAdmin(Order order, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        boolean isOwner = order.getUser() != null
                && order.getUser().getEmail().equalsIgnoreCase(authentication.getName());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You do not have permission to view this order");
        }
    }
}
