package com.shopdrop.service;

import com.shopdrop.model.Category;
import com.shopdrop.model.Product;
import com.shopdrop.repository.CategoryRepository;
import com.shopdrop.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/** Product catalog reads and search, the admin create/update/delete workflow, and stock adjustment. */
@Service
public class ProductService {

    private static final String DEFAULT_ICON = "\uD83D\uDCE6";

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public List<Product> findByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    /** Combines the optional category filter with an optional case-insensitive name search. */
    public List<Product> search(Long categoryId, String query) {
        List<Product> products = categoryId != null ? findByCategory(categoryId) : findAll();

        if (query == null || query.isBlank()) {
            return products;
        }

        String needle = query.trim().toLowerCase();
        return products.stream()
                .filter(product -> product.getName().toLowerCase().contains(needle))
                .collect(Collectors.toList());
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    public Product create(String name, String description, Long categoryId, BigDecimal price,
                           Integer stockQuantity, String icon) {
        Category category = fetchCategory(categoryId);
        validate(name, price, stockQuantity);

        Product product = new Product(name.trim(), description, category, price, stockQuantity,
                resolveIcon(icon, DEFAULT_ICON));
        return productRepository.save(product);
    }

    public Product update(Long id, String name, String description, Long categoryId, BigDecimal price,
                           Integer stockQuantity, String icon) {
        Product product = findById(id);
        Category category = fetchCategory(categoryId);
        validate(name, price, stockQuantity);

        product.setName(name.trim());
        product.setDescription(description);
        product.setCategory(category);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        product.setIcon(resolveIcon(icon, product.getIcon()));

        return productRepository.save(product);
    }

    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }

    /** Called when an order is placed. Fails loudly rather than allowing stock to go negative. */
    public void decreaseStock(Product product, int quantity) {
        int remaining = product.getStockQuantity() - quantity;
        if (remaining < 0) {
            throw new IllegalArgumentException("Not enough stock available for " + product.getName());
        }
        product.setStockQuantity(remaining);
        productRepository.save(product);
    }

    /** Called when an order is cancelled, to return its items to the shelf. */
    public void increaseStock(Product product, int quantity) {
        product.setStockQuantity(product.getStockQuantity() + quantity);
        productRepository.save(product);
    }

    private Category fetchCategory(Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("A category is required");
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
    }

    private void validate(String name, BigDecimal price, Integer stockQuantity) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        if (stockQuantity == null || stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
    }

    private String resolveIcon(String requestedIcon, String fallback) {
        return (requestedIcon == null || requestedIcon.isBlank()) ? fallback : requestedIcon.trim();
    }
}
