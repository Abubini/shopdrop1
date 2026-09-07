package com.shopdrop.system.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class OrderStatusPage extends BasePage {

    private static final By STATUS = By.id("order-status");
    private static final By CANCEL_BTN = By.id("cancel-order-btn");

    public OrderStatusPage(WebDriver driver) {
        super(driver);
    }

    public String getStatusText() {
        return waitFor(STATUS).getText();
    }

    public void cancelOrder() {
        WebElement previousStatus = driver.findElement(STATUS);
        click(CANCEL_BTN);
        wait.until(ExpectedConditions.stalenessOf(previousStatus));
    }
}