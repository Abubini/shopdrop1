package com.shopdrop.system.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ProductDetailPage extends BasePage {

    private static final By QUANTITY = By.id("quantity");
    private static final By ADD_TO_CART_BTN = By.id("add-to-cart-btn");

    public ProductDetailPage(WebDriver driver) {
        super(driver);
    }

    public void addToCart(int quantity) {
        type(QUANTITY, String.valueOf(quantity));
        click(ADD_TO_CART_BTN);
    }
}
