// src/test/java/com/shopdrop/system/pages/CartPage.java
package com.shopdrop.system.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CartPage extends BasePage {

    private static final By CHECKOUT_LINK = By.id("checkout-link");
    private static final By SUBTOTAL = By.id("cart-subtotal");

    public CartPage(WebDriver driver) {
        super(driver);
    }

    public CartPage open(String baseUrl) {
        driver.get(baseUrl + "/cart");
        return this;
    }

    public String getSubtotalText() {
        return waitFor(SUBTOTAL).getText();
    }

    public void goToCheckout() {
        click(CHECKOUT_LINK);
    }

    public boolean hasRowFor(Long productId) {
        return !driver.findElements(rowLocator(productId)).isEmpty();
    }

    public void updateQuantity(Long productId, int quantity) {
        By quantityInput = By.cssSelector("[data-testid='cart-row-" + productId + "'] input[name='quantity']");
        By updateButton = By.cssSelector("[data-testid='cart-row-" + productId + "'] form[action*='/cart/update'] button");
        type(quantityInput, String.valueOf(quantity));
        click(updateButton);
    }

    public void removeItem(Long productId) {
        By removeButton = By.cssSelector("[data-testid='cart-row-" + productId + "'] form[action*='/cart/remove'] button");
        click(removeButton);
    }

    private By rowLocator(Long productId) {
        return By.cssSelector("[data-testid='cart-row-" + productId + "']");
    }
}