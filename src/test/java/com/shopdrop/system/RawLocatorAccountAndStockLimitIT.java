// src/test/java/com/shopdrop/system/RawLocatorAccountAndStockLimitIT.java
package com.shopdrop.system;

import com.shopdrop.model.Product;
import com.shopdrop.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Account profile display and the stock-limit boundary, driven with raw WebDriver locators. */
class RawLocatorAccountAndStockLimitIT extends BaseSystemTest {

    @Autowired
    private ProductRepository productRepository;

    private void logInAsDemo(WebDriverWait wait) {
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys("demo@shopdrop.com");
        driver.findElement(By.id("password")).sendKeys("demo1234");
        driver.findElement(By.id("login-btn")).click();
    }

    @Test
    void accountPageShowsProfileDetails_viaRawLocators() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        logInAsDemo(wait);

        driver.get(baseUrl + "/account");
        WebElement nameCell = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("account-name")));
        assertTrue(nameCell.getText().contains("Demo Shopper"));

        WebElement emailCell = driver.findElement(By.id("account-email"));
        assertTrue(emailCell.getText().contains("demo@shopdrop.com"));

        WebElement roleCell = driver.findElement(By.id("account-role"));
        assertTrue(roleCell.getText().contains("USER"));
    }

    @Test
    void updatingCartQuantityBeyondAvailableStock_isRejectedViaRawLocators() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        Product product = productRepository.findAll().stream()
                .filter(p -> p.getName().equals("Standing Desk")) // low seeded stock: 8
                .findFirst()
                .orElseThrow();

        logInAsDemo(wait);

        // Add a single unit first -- well within stock, and within the product page's own
        // max attribute (which is tied to stock, so the browser itself would block anything
        // over that before the request ever reached the server).
        driver.get(baseUrl + "/products/" + product.getId());
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("quantity")));
        driver.findElement(By.id("add-to-cart-btn")).click();

        // The cart's own quantity field caps at a fixed 99, not at the product's stock, so
        // this is the one place we can submit a stock-exceeding value without client-side
        // HTML5 validation silently swallowing the request before the server ever sees it.
        driver.get(baseUrl + "/cart");
        By quantityInput = By.cssSelector("[data-testid='cart-row-" + product.getId() + "'] input[name='quantity']");
        WebElement quantityField = wait.until(ExpectedConditions.visibilityOfElementLocated(quantityInput));
        quantityField.clear();
        quantityField.sendKeys(String.valueOf(product.getStockQuantity() + 1));
        driver.findElement(
                By.cssSelector("[data-testid='cart-row-" + product.getId() + "'] form[action*='/cart/update'] button")
        ).click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("error-message")));
        assertTrue(errorMessage.getText().toLowerCase().contains("stock"),
                "Expected an out-of-stock style error message, was: " + errorMessage.getText());
    }
}