package com.shopdrop.system;

import com.shopdrop.system.pages.CartPage;
import com.shopdrop.system.pages.CheckoutPage;
import com.shopdrop.system.pages.LoginPage;
import com.shopdrop.system.pages.OrderStatusPage;
import com.shopdrop.system.pages.ProductDetailPage;
import com.shopdrop.system.pages.ProductsPage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** The multi-step user journey Selenium was brought in for: browse -> cart -> checkout -> track. */
class ShoppingJourneyIT extends BaseSystemTest {

    @Test
    void loggedInUserCanBrowseAddToCartAndCheckout() {
        new LoginPage(driver).open(baseUrl).loginAs("demo@shopdrop.com", "demo1234");

        new ProductsPage(driver).open(baseUrl).openProductByName("Ceramic Mug");
        new ProductDetailPage(driver).addToCart(2);

        CartPage cartPage = new CartPage(driver).open(baseUrl);
        assertTrue(cartPage.getSubtotalText().contains("24.00"),
                "Expected the cart subtotal for 2 mugs to include 24.00, was: " + cartPage.getSubtotalText());
        cartPage.goToCheckout();

        new CheckoutPage(driver).placeOrder();

        OrderStatusPage orderStatusPage = new OrderStatusPage(driver);
        assertTrue(orderStatusPage.getStatusText().contains("PLACED"));
    }

    @Test
    void anonymousVisitorCanBrowseButIsSentToLoginWhenAddingToCart() {
        new ProductsPage(driver).open(baseUrl).openProductByName("Wireless Earbuds");
        new ProductDetailPage(driver).addToCart(1);

        assertTrue(driver.getCurrentUrl().contains("/login"),
                "Expected an anonymous add-to-cart to redirect to /login, was: " + driver.getCurrentUrl());
    }
}
