package com.shopdrop.service;

import com.shopdrop.model.Product;
import com.shopdrop.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds the shopping cart for the current HTTP session: a map of product id -> quantity,
 * resolved against the product catalog on read.
 */
@Service
@SessionScope
public class CartService {

    private final ProductRepository productRepository;
    private final Map<Long, Integer> items = new LinkedHashMap<>();

    public CartService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void addItem(Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        int currentQuantity = items.getOrDefault(productId, 0);
        int requestedTotal = currentQuantity + quantity;
        if (requestedTotal > product.getStockQuantity()) {
            throw new IllegalArgumentException(
                    "Only " + product.getStockQuantity() + " left in stock for " + product.getName());
        }

        items.put(productId, requestedTotal);
    }

    public void removeItem(Long productId) {
        items.remove(productId);
    }

    public void updateQuantity(Long productId, int quantity) {
        if (quantity <= 0) {
            items.remove(productId);
            return;
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        if (quantity > product.getStockQuantity()) {
            throw new IllegalArgumentException(
                    "Only " + product.getStockQuantity() + " left in stock for " + product.getName());
        }

        items.put(productId, quantity);
    }

    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public List<CartLine> getLines() {
        List<CartLine> lines = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : items.entrySet()) {
            productRepository.findById(entry.getKey())
                    .ifPresent(product -> lines.add(new CartLine(product, entry.getValue())));
        }
        return lines;
    }

    public BigDecimal getSubtotal() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartLine line : getLines()) {
            subtotal = subtotal.add(line.getLineTotal());
        }
        return subtotal;
    }

    public static class CartLine {
        private final Product product;
        private final int quantity;

        public CartLine(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public Product getProduct() {
            return product;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getLineTotal() {
            return product.getPrice().multiply(BigDecimal.valueOf(quantity));
        }
    }
}
