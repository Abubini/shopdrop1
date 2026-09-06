package com.shopdrop.controller;

import com.shopdrop.model.Product;
import com.shopdrop.model.User;
import com.shopdrop.repository.UserRepository;
import com.shopdrop.service.ProductService;
import com.shopdrop.service.ReviewService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReviewController {

    private final ReviewService reviewService;
    private final ProductService productService;
    private final UserRepository userRepository;

    public ReviewController(ReviewService reviewService, ProductService productService,
                             UserRepository userRepository) {
        this.reviewService = reviewService;
        this.productService = productService;
        this.userRepository = userRepository;
    }

    @PostMapping("/products/{id}/reviews")
    public String submit(@PathVariable Long id,
                          @RequestParam int rating,
                          @RequestParam(required = false) String comment,
                          Authentication authentication) {
        Product product = productService.findById(id);
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + authentication.getName()));

        reviewService.submitReview(user, product, rating, comment);
        return "redirect:/products/" + id;
    }
}
