package com.shopdrop.controller;

import com.shopdrop.model.Product;
import com.shopdrop.repository.CategoryRepository;
import com.shopdrop.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/** Admin-only product management: create, edit and delete catalog items. */
@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    public AdminProductController(ProductService productService, CategoryRepository categoryRepository) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productService.findAll());
        return "admin-products";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("formAction", "/admin/products");
        return "admin-product-form";
    }

    @PostMapping
    public String create(@RequestParam String name,
                          @RequestParam String description,
                          @RequestParam Long categoryId,
                          @RequestParam BigDecimal price,
                          @RequestParam Integer stockQuantity,
                          @RequestParam(required = false) String icon) {
        productService.create(name, description, categoryId, price, stockQuantity, icon);
        return "redirect:/admin/products";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.findById(id));
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("formAction", "/admin/products/" + id);
        return "admin-product-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                          @RequestParam String name,
                          @RequestParam String description,
                          @RequestParam Long categoryId,
                          @RequestParam BigDecimal price,
                          @RequestParam Integer stockQuantity,
                          @RequestParam(required = false) String icon) {
        productService.update(id, name, description, categoryId, price, stockQuantity, icon);
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        productService.delete(id);
        return "redirect:/admin/products";
    }
}
