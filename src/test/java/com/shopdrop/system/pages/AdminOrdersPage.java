package com.shopdrop.system.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class AdminOrdersPage extends BasePage {

    public AdminOrdersPage(WebDriver driver) {
        super(driver);
    }

    public AdminOrdersPage open(String baseUrl) {
        driver.get(baseUrl + "/admin/orders");
        return this;
    }

    public void advanceOrder(Long orderId) {
        click(By.id("advance-btn-" + orderId));
    }

    public void cancelOrder(Long orderId) {
        click(By.id("cancel-btn-" + orderId));
    }

    public String getStatusFor(Long orderId) {
        return waitFor(By.cssSelector("[data-testid='admin-order-row-" + orderId + "'] .status-badge")).getText();
    }
}
