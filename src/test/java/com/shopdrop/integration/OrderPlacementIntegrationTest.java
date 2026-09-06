package com.shopdrop.integration;

import com.shopdrop.model.Order;
import com.shopdrop.model.Product;
import com.shopdrop.repository.OrderRepository;
import com.shopdrop.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises CartService, CheckoutController, OrderService, ProductService and the JPA
 * repositories together: add a product to the (session-scoped) cart, then check out, then
 * verify what was persisted -- including that the purchased stock was actually decremented.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OrderPlacementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @WithUserDetails("demo@shopdrop.com")
    void addingToCartAndCheckingOut_persistsAnOrderWithCorrectTotalsAndDecrementsStock() throws Exception {
        long before = orderRepository.count();
        MockHttpSession session = new MockHttpSession();

        // Product id 1 is the first product DataLoader seeds: Wireless Earbuds, $15.99, stock 50.
        int stockBefore = productRepository.findById(1L).orElseThrow().getStockQuantity();

        mockMvc.perform(post("/cart/add").with(csrf()).session(session)
                        .param("productId", "1")
                        .param("quantity", "1"))
                .andExpect(status().is3xxRedirection());

        MvcResult result = mockMvc.perform(post("/checkout/place").with(csrf()).session(session))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String redirectedUrl = result.getResponse().getRedirectedUrl();
        assertTrue(redirectedUrl != null && redirectedUrl.startsWith("/orders/"));
        assertEquals(before + 1, orderRepository.count());

        Long orderId = Long.valueOf(redirectedUrl.substring(redirectedUrl.lastIndexOf('/') + 1));
        Order order = orderRepository.findById(orderId).orElseThrow();

        assertEquals(new BigDecimal("15.99"), order.getSubtotal());
        assertEquals("demo@shopdrop.com", order.getCustomerEmail());
        assertEquals("demo@shopdrop.com", order.getUser().getEmail());

        Product afterPurchase = productRepository.findById(1L).orElseThrow();
        assertEquals(stockBefore - 1, afterPurchase.getStockQuantity());
    }

    @Test
    @WithUserDetails("demo@shopdrop.com")
    void addingMoreThanAvailableStockToCart_isRejected() throws Exception {
        MockHttpSession session = new MockHttpSession();
        int stock = productRepository.findById(1L).orElseThrow().getStockQuantity();

        mockMvc.perform(post("/cart/add").with(csrf()).session(session)
                        .param("productId", "1")
                        .param("quantity", String.valueOf(stock + 1)))
                .andExpect(status().isOk()); // rejected with the generic error view, not a redirect
    }
}
