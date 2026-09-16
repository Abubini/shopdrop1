// src/test/java/com/shopdrop/system/RawLocatorCheckoutOrderLifecycleIT.java
package com.shopdrop.system;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Checkout through cancellation, driven entirely with raw WebDriver locators, no Page Objects. */
class RawLocatorCheckoutOrderLifecycleIT extends BaseSystemTest {

    @Test
    void placingAndThenCancellingAnOrder_worksViaRawLocators() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(50));

        // --- log in ---
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys("demo@shopdrop.com");
        driver.findElement(By.id("password")).sendKeys("demo1234");
        driver.findElement(By.id("login-btn")).click();

        // --- browse straight to the first product card and add it to the cart ---
        driver.get(baseUrl + "/products");
        WebElement firstCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#product-grid .product-card")));
        firstCard.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("add-to-cart-btn"))).click();

        // --- go to checkout and place the order ---
        driver.get(baseUrl + "/cart");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("checkout-link"))).click();

        WebElement total = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("summary-total")));
        assertTrue(total.getText().startsWith("$"));

        driver.findElement(By.id("place-order-btn")).click();

        // --- now on the order status page: confirm PLACED, then cancel ---
        WebElement status = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("order-status")));
        assertTrue(status.getText().contains("PLACED"));

        driver.findElement(By.id("cancel-order-btn")).click();

        WebElement statusAfterCancel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("order-status")));
        assertTrue(statusAfterCancel.getText().contains("CANCELLED"));
        assertTrue(driver.findElements(By.id("cancel-order-btn")).isEmpty(),
                "Expected the cancel button to be gone once the order is already cancelled");
    }
}