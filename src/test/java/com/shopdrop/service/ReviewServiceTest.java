package com.shopdrop.service;

import com.shopdrop.model.Order;
import com.shopdrop.model.OrderItem;
import com.shopdrop.model.OrderStatus;
import com.shopdrop.model.Product;
import com.shopdrop.model.Review;
import com.shopdrop.model.User;
import com.shopdrop.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Tests the review eligibility rule ("bought it, and it was delivered, and hasn't reviewed it
 * yet") and rating validation, with mocked collaborators.
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderService orderService;

    private ReviewService reviewService;
    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewRepository, orderService);
        user = new User();
        user.setId(1L);
        product = new Product();
        product.setId(10L);
        lenient().when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Order orderContaining(Product p, OrderStatus status) {
        Order order = new Order();
        order.setStatus(status);
        OrderItem item = new OrderItem();
        item.setProduct(p);
        order.addItem(item);
        return order;
    }

    @Test
    void hasPurchasedAndReceived_withDeliveredOrderContainingProduct_isTrue() {
        when(orderService.findByUser(user)).thenReturn(List.of(orderContaining(product, OrderStatus.DELIVERED)));
        assertTrue(reviewService.hasPurchasedAndReceived(user, product));
    }

    @Test
    void hasPurchasedAndReceived_withOnlyAPlacedOrder_isFalse() {
        when(orderService.findByUser(user)).thenReturn(List.of(orderContaining(product, OrderStatus.PLACED)));
        assertFalse(reviewService.hasPurchasedAndReceived(user, product));
    }

    @Test
    void hasPurchasedAndReceived_withNoOrders_isFalse() {
        when(orderService.findByUser(user)).thenReturn(List.of());
        assertFalse(reviewService.hasPurchasedAndReceived(user, product));
    }

    @Test
    void hasPurchasedAndReceived_withDeliveredOrderForADifferentProduct_isFalse() {
        Product otherProduct = new Product();
        otherProduct.setId(99L);
        when(orderService.findByUser(user)).thenReturn(List.of(orderContaining(otherProduct, OrderStatus.DELIVERED)));

        assertFalse(reviewService.hasPurchasedAndReceived(user, product));
    }

    @Test
    void submitReview_whenPurchasedAndDelivered_succeeds() {
        when(orderService.findByUser(user)).thenReturn(List.of(orderContaining(product, OrderStatus.DELIVERED)));
        when(reviewRepository.existsByUserIdAndProductId(1L, 10L)).thenReturn(false);

        Review review = reviewService.submitReview(user, product, 5, "Great!");

        assertEquals(5, review.getRating());
        assertEquals("Great!", review.getComment());
    }

    @Test
    void submitReview_withoutAnyPurchase_throwsException() {
        when(orderService.findByUser(user)).thenReturn(List.of());
        assertThrows(IllegalArgumentException.class, () -> reviewService.submitReview(user, product, 5, "Nice"));
    }

    @Test
    void submitReview_withUndeliveredOrder_throwsException() {
        when(orderService.findByUser(user)).thenReturn(List.of(orderContaining(product, OrderStatus.PLACED)));
        assertThrows(IllegalArgumentException.class, () -> reviewService.submitReview(user, product, 5, "Nice"));
    }

    @Test
    void submitReview_whenAlreadyReviewed_throwsException() {
        when(orderService.findByUser(user)).thenReturn(List.of(orderContaining(product, OrderStatus.DELIVERED)));
        when(reviewRepository.existsByUserIdAndProductId(1L, 10L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> reviewService.submitReview(user, product, 5, "Nice"));
    }

    @ParameterizedTest(name = "invalid rating: {0}")
    @ValueSource(ints = {0, 6, -1})
    void submitReview_withRatingOutsideOneToFive_throwsException(int badRating) {
        assertThrows(IllegalArgumentException.class, () -> reviewService.submitReview(user, product, badRating, "x"));
    }

    @ParameterizedTest(name = "boundary rating: {0}")
    @ValueSource(ints = {1, 5})
    void submitReview_withBoundaryRatings_succeeds(int boundaryRating) {
        when(orderService.findByUser(user)).thenReturn(List.of(orderContaining(product, OrderStatus.DELIVERED)));
        when(reviewRepository.existsByUserIdAndProductId(1L, 10L)).thenReturn(false);

        Review review = reviewService.submitReview(user, product, boundaryRating, "ok");
        assertEquals(boundaryRating, review.getRating());
    }

    @Test
    void averageRating_computesMeanOfAllReviews() {
        Review r1 = new Review();
        r1.setRating(4);
        Review r2 = new Review();
        r2.setRating(2);
        when(reviewRepository.findByProductIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(r1, r2));

        assertEquals(3.0, reviewService.averageRating(product));
    }

    @Test
    void averageRating_withNoReviews_returnsZero() {
        when(reviewRepository.findByProductIdOrderByCreatedAtDesc(10L)).thenReturn(List.of());
        assertEquals(0.0, reviewService.averageRating(product));
    }
}
