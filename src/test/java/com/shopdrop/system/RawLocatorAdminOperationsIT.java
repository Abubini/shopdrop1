// src/test/java/com/shopdrop/system/RawLocatorAdminOperationsIT.java
package com.shopdrop.system;

import com.shopdrop.model.Order;
import com.shopdrop.model.OrderItem;
import com.shopdrop.model.Product;
import com.shopdrop.model.User;
import com.shopdrop.repository.ProductRepository;
import com.shopdrop.repository.UserRepository;
import com.shopdrop.service.OrderService;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Admin dashboard, full product CRUD (including the delete confirm() dialog) and order status
 * advancement -- all through raw WebDriver locators instead of the Page Object classes.
 */
class RawLocatorAdminOperationsIT extends BaseSystemTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void adminCanViewDashboardAndManageAProductLifecycle_viaRawLocators() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // --- log in as admin ---
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys("admin@shopdrop.com");
        driver.findElement(By.id("password")).sendKeys("admin123");
        driver.findElement(By.id("login-btn")).click();

        // --- dashboard should show numeric stats ---
        WebElement productCount = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("stat-total-products")));
        int productsBefore = Integer.parseInt(productCount.getText().trim());
        assertTrue(productsBefore > 0);

        // --- create a new product ---
        driver.get(baseUrl + "/admin/products");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("new-product-btn"))).click();

        String productName = "Raw Locator Gadget " + System.currentTimeMillis();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name"))).sendKeys(productName);
        driver.findElement(By.id("description")).sendKeys("Created via raw locators");
        new Select(driver.findElement(By.id("categoryId"))).selectByVisibleText("Electronics");
        driver.findElement(By.id("price")).sendKeys("19.99");
        driver.findElement(By.id("stockQuantity")).sendKeys("15");
        driver.findElement(By.id("save-product-btn")).click();

        WebElement productsTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("admin-products-table")));
        assertTrue(productsTable.getText().contains(productName));

        // --- the dashboard total should have gone up by one ---
        driver.get(baseUrl + "/admin");
        WebElement productCountAfterCreate = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("stat-total-products")));
        int productsAfterCreate = Integer.parseInt(productCountAfterCreate.getText().trim());
        assertTrue(productsAfterCreate == productsBefore + 1,
                "Expected the product count to increase by 1, was " + productsBefore + " -> " + productsAfterCreate);

        // --- edit the product we just made ---
        driver.get(baseUrl + "/admin/products");
        WebElement row = driver.findElement(By.xpath("//tr[td[contains(text(), '" + productName + "')]]"));
        row.findElement(By.cssSelector("a[href*='/edit']")).click();

        WebElement nameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name")));
        nameField.clear();
        String updatedName = productName + " (Updated)";
        nameField.sendKeys(updatedName);
        driver.findElement(By.id("save-product-btn")).click();

        WebElement productsTableAfterEdit = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("admin-products-table")));
        assertTrue(productsTableAfterEdit.getText().contains(updatedName));

        // --- delete it -- the Delete button fires a native confirm() dialog, so accept it ---
        WebElement updatedRow = driver.findElement(By.xpath("//tr[td[contains(text(), '" + updatedName + "')]]"));
        updatedRow.findElement(By.cssSelector("form[action*='/delete'] button")).click();
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//tr[td[contains(text(), '" + updatedName + "')]]")));
        assertFalse(driver.getPageSource().contains(updatedName));
    }

    @Test
    void adminCanAdvanceAnOrderAndSeeItReflectedOnTheDashboard_viaRawLocators() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        User demo = userRepository.findByEmail("demo@shopdrop.com").orElseThrow();
        Product product = productRepository.findAll().get(0);

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setProductName(product.getName());
        item.setUnitPrice(new BigDecimal("10.00"));
        item.setQuantity(1);
        item.setLineTotal(new BigDecimal("10.00"));
        Order order = orderService.placeOrder(demo, List.of(item));

        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys("admin@shopdrop.com");
        driver.findElement(By.id("password")).sendKeys("admin123");
        driver.findElement(By.id("login-btn")).click();

        driver.get(baseUrl + "/admin/orders");
        By statusBadge = By.cssSelector("[data-testid='admin-order-row-" + order.getId() + "'] .status-badge");
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(statusBadge));
        assertTrue(badge.getText().contains("PLACED"));

        driver.findElement(By.id("advance-btn-" + order.getId())).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(statusBadge));
        WebElement badgeAfterAdvance = driver.findElement(statusBadge);
        assertTrue(badgeAfterAdvance.getText().contains("PACKED"),
                "Expected PACKED after one advance, was: " + badgeAfterAdvance.getText());
    }
}