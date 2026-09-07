// src/test/java/com/shopdrop/system/OrderCancellationIT.java
package com.shopdrop.system;

import com.shopdrop.model.Order;
import com.shopdrop.model.OrderItem;
import com.shopdrop.model.Product;
import com.shopdrop.model.User;
import com.shopdrop.repository.ProductRepository;
import com.shopdrop.repository.UserRepository;
import com.shopdrop.service.OrderService;
import com.shopdrop.system.pages.LoginPage;
import com.shopdrop.system.pages.OrderStatusPage;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Covers cancelling an order through the tracking page, which ShoppingJourneyIT doesn't exercise. */
class OrderCancellationIT extends BaseSystemTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void userCanCancelAPlacedOrderThroughTheUi() {
        User demo = userRepository.findByEmail("demo@shopdrop.com").orElseThrow();
        Product product = productRepository.findAll().get(0);

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setProductName(product.getName());
        item.setUnitPrice(product.getPrice());
        item.setQuantity(1);
        item.setLineTotal(product.getPrice());

        Order order = orderService.placeOrder(demo, List.of(item));

        new LoginPage(driver).open(baseUrl).loginAs("demo@shopdrop.com", "demo1234");

        driver.get(baseUrl + "/orders/" + order.getId());
        OrderStatusPage orderStatusPage = new OrderStatusPage(driver);
        assertTrue(orderStatusPage.getStatusText().contains("PLACED"));

        orderStatusPage.cancelOrder();

        orderStatusPage = new OrderStatusPage(driver);
        assertTrue(orderStatusPage.getStatusText().contains("CANCELLED"));
        assertTrue(driver.findElements(By.id("cancel-order-btn")).isEmpty(),
                "Expected the cancel button to disappear once the order is already cancelled");
    }
}