package com.shopdrop.service;

import com.shopdrop.model.Order;
import com.shopdrop.model.OrderStatus;
import com.shopdrop.model.Product;
import com.shopdrop.model.Review;
import com.shopdrop.model.User;
import com.shopdrop.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A customer may only review a product they actually bought and received: an order containing
 * that product must exist for them with status DELIVERED, and they can only review it once.
 */
@Service
public class ReviewService {

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    private final ReviewRepository reviewRepository;
    private final OrderService orderService;

    public ReviewService(ReviewRepository reviewRepository, OrderService orderService) {
        this.reviewRepository = reviewRepository;
        this.orderService = orderService;
    }

    @Transactional(readOnly = true)
    public boolean hasPurchasedAndReceived(User user, Product product) {
        return orderService.findByUser(user).stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .flatMap(order -> order.getItems().stream())
                .anyMatch(item -> item.getProduct() != null && item.getProduct().getId().equals(product.getId()));
    }

    public boolean hasAlreadyReviewed(User user, Product product) {
        return reviewRepository.existsByUserIdAndProductId(user.getId(), product.getId());
    }

    @Transactional
    public Review submitReview(User user, Product product, int rating, String comment) {
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new IllegalArgumentException("Rating must be between " + MIN_RATING + " and " + MAX_RATING);
        }
        if (!hasPurchasedAndReceived(user, product)) {
            throw new IllegalArgumentException("You can only review products from orders that have been delivered to you");
        }
        if (hasAlreadyReviewed(user, product)) {
            throw new IllegalArgumentException("You have already reviewed this product");
        }

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    public List<Review> findByProduct(Product product) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(product.getId());
    }

    public double averageRating(Product product) {
        List<Review> reviews = findByProduct(product);
        if (reviews.isEmpty()) {
            return 0.0;
        }
        double sum = 0;
        for (Review review : reviews) {
            sum += review.getRating();
        }
        return sum / reviews.size();
    }
}