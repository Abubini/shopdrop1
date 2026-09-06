package com.shopdrop.controller;

import com.shopdrop.model.Order;
import com.shopdrop.model.OrderItem;
import com.shopdrop.model.User;
import com.shopdrop.repository.UserRepository;
import com.shopdrop.service.CartService;
import com.shopdrop.service.DiscountService;
import com.shopdrop.service.OrderService;
import com.shopdrop.service.ShippingFeeCalculator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final DiscountService discountService;
    private final ShippingFeeCalculator shippingFeeCalculator;
    private final UserRepository userRepository;

    public CheckoutController(CartService cartService, OrderService orderService,
                               DiscountService discountService, ShippingFeeCalculator shippingFeeCalculator,
                               UserRepository userRepository) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.discountService = discountService;
        this.shippingFeeCalculator = shippingFeeCalculator;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String show(Authentication authentication, Model model) {
        if (cartService.isEmpty()) {
            return "redirect:/cart";
        }

        User user = currentUser(authentication);

        BigDecimal subtotal = cartService.getSubtotal();
        BigDecimal discount = discountService.calculateDiscountAmount(subtotal, user.isMember());
        BigDecimal shipping = shippingFeeCalculator.calculate(subtotal);
        BigDecimal total = subtotal.subtract(discount).add(shipping);

        model.addAttribute("lines", cartService.getLines());
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("discount", discount);
        model.addAttribute("shipping", shipping);
        model.addAttribute("total", total);
        model.addAttribute("member", user.isMember());
        return "checkout";
    }

    @PostMapping("/place")
    public String place(Authentication authentication) {
        User user = currentUser(authentication);

        List<OrderItem> items = new ArrayList<>();
        for (CartService.CartLine line : cartService.getLines()) {
            OrderItem item = new OrderItem();
            item.setProduct(line.getProduct());
            item.setProductName(line.getProduct().getName());
            item.setUnitPrice(line.getProduct().getPrice());
            item.setQuantity(line.getQuantity());
            item.setLineTotal(line.getLineTotal());
            items.add(item);
        }

        Order order = orderService.placeOrder(user, items);
        cartService.clear();
        return "redirect:/orders/" + order.getId();
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + authentication.getName()));
    }
}
