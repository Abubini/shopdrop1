// src/test/java/com/shopdrop/system/RawLocatorShoppingAndCartIT.java
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Search, category-card lookup, add-to-cart, update quantity and remove -- all through raw
 * WebDriver calls (By.id / By.cssSelector) instead of the Page Object classes.
 */
class RawLocatorShoppingAndCartIT extends BaseSystemTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void searchAddUpdateAndRemoveCartLines_allWorkViaRawLocators() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        Product mug = productRepository.findAll().stream()
                .filter(p -> p.getName().equals("Ceramic Mug"))
                .findFirst()
                .orElseThrow();

        // --- log in ---
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys("demo@shopdrop.com");
        driver.findElement(By.id("password")).sendKeys("demo1234");
        driver.findElement(By.id("login-btn")).click();

        // --- search the catalog ---
        driver.get(baseUrl + "/products");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("search-input"))).sendKeys("Mug");
        driver.findElement(By.id("search-btn")).click();

        WebElement grid = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("product-grid")));
        assertTrue(grid.getText().contains("Ceramic Mug"));

        // --- open the product and add it to the cart ---
        driver.findElement(By.cssSelector("[data-testid='product-card-" + mug.getId() + "']")).click();
        WebElement quantityField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("quantity")));
        quantityField.clear();
        quantityField.sendKeys("2");
        driver.findElement(By.id("add-to-cart-btn")).click();

        // --- verify the cart row and subtotal, then update the quantity ---
        driver.get(baseUrl + "/cart");
        By cartRow = By.cssSelector("[data-testid='cart-row-" + mug.getId() + "']");
        wait.until(ExpectedConditions.visibilityOfElementLocated(cartRow));

        String subtotalBefore = driver.findElement(By.id("cart-subtotal")).getText();

        WebElement quantityInput = driver.findElement(
                By.cssSelector("[data-testid='cart-row-" + mug.getId() + "'] input[name='quantity']"));
        quantityInput.clear();
        quantityInput.sendKeys("5");
        driver.findElement(
                By.cssSelector("[data-testid='cart-row-" + mug.getId() + "'] form[action*='/cart/update'] button")
        ).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cart-subtotal")));
        String subtotalAfterUpdate = driver.findElement(By.id("cart-subtotal")).getText();
        assertFalse(subtotalAfterUpdate.equals(subtotalBefore),
                "Expected the subtotal to change after updating the quantity to 5, both were: " + subtotalAfterUpdate);

        // --- remove the line entirely ---
        driver.findElement(
                By.cssSelector("[data-testid='cart-row-" + mug.getId() + "'] form[action*='/cart/remove'] button")
        ).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("empty-cart")));
        assertTrue(driver.findElements(cartRow).isEmpty(), "Expected the cart row to be gone after removing it");
    }
}