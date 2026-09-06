package com.shopdrop.system;

import com.shopdrop.model.Order;
import com.shopdrop.model.OrderItem;
import com.shopdrop.model.Product;
import com.shopdrop.model.User;
import com.shopdrop.repository.ProductRepository;
import com.shopdrop.repository.UserRepository;
import com.shopdrop.service.OrderService;
import com.shopdrop.system.pages.AdminOrdersPage;
import com.shopdrop.system.pages.LoginPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Drives the order state machine through the real UI: an admin advancing an order
 * PLACED -> PACKED -> SHIPPED via the warehouse screen.
 */
class AdminOrderManagementIT extends BaseSystemTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void adminCanAdvanceAnOrderThroughItsLifecycle() {
        User demo = userRepository.findByEmail("demo@shopdrop.com").orElseThrow();
        Product product = productRepository.findAll().get(0);

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setProductName(product.getName());
        item.setUnitPrice(new BigDecimal("10.00"));
        item.setQuantity(1);
        item.setLineTotal(new BigDecimal("10.00"));

        Order order = orderService.placeOrder(demo, List.of(item));

        new LoginPage(driver).open(baseUrl).loginAs("admin@shopdrop.com", "admin123");

        AdminOrdersPage adminOrdersPage = new AdminOrdersPage(driver).open(baseUrl);

        adminOrdersPage.advanceOrder(order.getId());
        assertEquals("PACKED", adminOrdersPage.getStatusFor(order.getId()));

        adminOrdersPage.advanceOrder(order.getId());
        assertEquals("SHIPPED", adminOrdersPage.getStatusFor(order.getId()));
    }
}
