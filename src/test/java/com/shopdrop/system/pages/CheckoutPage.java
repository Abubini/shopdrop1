package com.shopdrop.system.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CheckoutPage extends BasePage {

    private static final By PLACE_ORDER_BTN = By.id("place-order-btn");
    private static final By TOTAL = By.id("summary-total");

    public CheckoutPage(WebDriver driver) {
        super(driver);
    }

    public String getTotalText() {
        return waitFor(TOTAL).getText();
    }

    public void placeOrder() {
        click(PLACE_ORDER_BTN);
    }
}
