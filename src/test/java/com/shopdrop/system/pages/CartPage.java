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
}
