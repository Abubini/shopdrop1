package com.shopdrop.controller;

import com.shopdrop.model.Product;
import com.shopdrop.model.User;
import com.shopdrop.repository.CategoryRepository;
import com.shopdrop.repository.UserRepository;
import com.shopdrop.service.ProductService;
import com.shopdrop.service.ReviewService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final ReviewService reviewService;
    private final UserRepository userRepository;

    public ProductController(ProductService productService, CategoryRepository categoryRepository,
                              ReviewService reviewService, UserRepository userRepository) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
        this.reviewService = reviewService;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/products";
    }

    @GetMapping("/products")
    public String list(@RequestParam(required = false) Long categoryId,
                        @RequestParam(required = false) String q,
                        Model model) {
        List<Product> products = productService.search(categoryId, q);

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("query", q);
        return "products";
    }

    @GetMapping("/products/{id}")
    public String detail(@PathVariable Long id, Authentication authentication, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        model.addAttribute("reviews", reviewService.findByProduct(product));
        model.addAttribute("averageRating", reviewService.averageRating(product));

        boolean canReview = false;
        boolean alreadyReviewed = false;
        User currentUser = resolveCurrentUser(authentication);
        if (currentUser != null) {
            alreadyReviewed = reviewService.hasAlreadyReviewed(currentUser, product);
            canReview = !alreadyReviewed && reviewService.hasPurchasedAndReceived(currentUser, product);
        }
        model.addAttribute("canReview", canReview);
        model.addAttribute("alreadyReviewed", alreadyReviewed);

        return "product-detail";
    }

    private User resolveCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return userRepository.findByEmail(authentication.getName()).orElse(null);
    }
}
