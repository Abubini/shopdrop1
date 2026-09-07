// src/test/java/com/shopdrop/system/CartManagementIT.java
package com.shopdrop.system;

import com.shopdrop.model.Product;
import com.shopdrop.repository.ProductRepository;
import com.shopdrop.system.pages.CartPage;
import com.shopdrop.system.pages.LoginPage;
import com.shopdrop.system.pages.ProductDetailPage;
import com.shopdrop.system.pages.ProductsPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Covers the cart update/remove flows that ShoppingJourneyIT doesn't exercise. */
class CartManagementIT extends BaseSystemTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void userCanUpdateAndRemoveCartLines() {
        new LoginPage(driver).open(baseUrl).loginAs("demo@shopdrop.com", "demo1234");

        Product mug = productRepository.findAll().stream()
                .filter(p -> p.getName().equals("Ceramic Mug"))
                .findFirst()
                .orElseThrow();
        Product bottle = productRepository.findAll().stream()
                .filter(p -> p.getName().equals("Water Bottle"))
                .findFirst()
                .orElseThrow();

        new ProductsPage(driver).open(baseUrl).openProductByName("Ceramic Mug");
        new ProductDetailPage(driver).addToCart(1);

        new ProductsPage(driver).open(baseUrl).openProductByName("Water Bottle");
        new ProductDetailPage(driver).addToCart(1);

        CartPage cartPage = new CartPage(driver).open(baseUrl);
        assertTrue(cartPage.hasRowFor(mug.getId()), "Expected a cart row for the mug");
        assertTrue(cartPage.hasRowFor(bottle.getId()), "Expected a cart row for the bottle");
        String subtotalWithBoth = cartPage.getSubtotalText();

        cartPage.updateQuantity(mug.getId(), 4);
        cartPage = new CartPage(driver).open(baseUrl);
        String subtotalAfterUpdate = cartPage.getSubtotalText();
        assertFalse(subtotalAfterUpdate.equals(subtotalWithBoth),
                "Expected the subtotal to change after increasing the mug quantity, both were: " + subtotalAfterUpdate);

        cartPage.removeItem(bottle.getId());
        cartPage = new CartPage(driver).open(baseUrl);
        assertFalse(cartPage.hasRowFor(bottle.getId()), "Expected the bottle row to be gone after removing it");
        assertTrue(cartPage.hasRowFor(mug.getId()), "Expected the mug row to remain after removing only the bottle");
    }
}