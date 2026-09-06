package com.shopdrop.integration;

import com.shopdrop.model.Order;
import com.shopdrop.model.OrderItem;
import com.shopdrop.model.Product;
import com.shopdrop.model.User;
import com.shopdrop.repository.ProductRepository;
import com.shopdrop.repository.ReviewRepository;
import com.shopdrop.repository.UserRepository;
import com.shopdrop.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the review eligibility rule through the real stack: ReviewController,
 * ReviewService, OrderService and the JPA repositories together.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ReviewIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private Order placeAndDeliverAnOrderFor(User user, Product product) {
        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setProductName(product.getName());
        item.setUnitPrice(product.getPrice());
        item.setQuantity(1);
        item.setLineTotal(product.getPrice());

        Order order = orderService.placeOrder(user, List.of(item));
        orderService.advance(order.getId()); // PACKED
        orderService.advance(order.getId()); // SHIPPED
        orderService.advance(order.getId()); // DELIVERED
        return order;
    }

    @Test
    @WithUserDetails("demo@shopdrop.com")
    void userWithADeliveredOrder_canSubmitAReview() throws Exception {
        User demo = userRepository.findByEmail("demo@shopdrop.com").orElseThrow();
        Product product = productRepository.findAll().stream()
                .filter(p -> p.getName().equals("Notebook Set"))
                .findFirst()
                .orElseThrow();

        placeAndDeliverAnOrderFor(demo, product);

        mockMvc.perform(post("/products/" + product.getId() + "/reviews").with(csrf())
                        .param("rating", "5")
                        .param("comment", "Great notebooks!"))
                .andExpect(status().is3xxRedirection());

        assertTrue(reviewRepository.existsByUserIdAndProductId(demo.getId(), product.getId()));
    }

    @Test
    @WithUserDetails("demo@shopdrop.com")
    void userWithoutAPurchase_cannotSubmitAReview() throws Exception {
        Product product = productRepository.findAll().stream()
                .filter(p -> p.getName().equals("Desk Lamp"))
                .findFirst()
                .orElseThrow();
        User demo = userRepository.findByEmail("demo@shopdrop.com").orElseThrow();

        mockMvc.perform(post("/products/" + product.getId() + "/reviews").with(csrf())
                        .param("rating", "5")
                        .param("comment", "Never bought this"))
                .andExpect(status().isOk()); // falls through to the generic error view

        assertFalse(reviewRepository.existsByUserIdAndProductId(demo.getId(), product.getId()));
    }

    @Test
    void anonymousUser_isRedirectedToLoginWhenSubmittingAReview() throws Exception {
        Product product = productRepository.findAll().get(0);

        mockMvc.perform(post("/products/" + product.getId() + "/reviews").with(csrf())
                        .param("rating", "5"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithUserDetails("demo@shopdrop.com")
    void submittingTheSameReviewTwice_isRejectedTheSecondTime() throws Exception {
        User demo = userRepository.findByEmail("demo@shopdrop.com").orElseThrow();
        Product product = productRepository.findAll().stream()
                .filter(p -> p.getName().equals("Water Bottle"))
                .findFirst()
                .orElseThrow();

        placeAndDeliverAnOrderFor(demo, product);

        mockMvc.perform(post("/products/" + product.getId() + "/reviews").with(csrf())
                        .param("rating", "4")
                        .param("comment", "Good bottle"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/products/" + product.getId() + "/reviews").with(csrf())
                        .param("rating", "5")
                        .param("comment", "Trying again"))
                .andExpect(status().isOk()); // second attempt hits the error view, not a redirect
    }
}
