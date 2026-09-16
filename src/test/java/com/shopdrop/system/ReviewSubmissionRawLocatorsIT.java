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

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The same kind of end-to-end check as the other system tests, but written with raw
 * By locators called directly on the WebDriver instead of going through the Page Object
 * classes in the .pages package. Useful as a point of comparison between the two styles:
 * Page Objects pay off once several tests reuse the same page, while a one-off flow like
 * this can be written directly against the driver without the extra abstraction.
 */
class ReviewSubmissionRawLocatorsIT extends BaseSystemTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void userWithADeliveredOrder_canLeaveAReviewThroughTheRawUi() {
        User demo = userRepository.findByEmail("demo@shopdrop.com").orElseThrow();
        Product product = productRepository.findAll().stream()
                .filter(p -> p.getName().equals("Backpack"))
                .findFirst()
                .orElseThrow();

        // Set up a delivered order for this user/product directly through the service layer,
        // so the test only has to drive the browser for the part it's actually checking.
        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setProductName(product.getName());
        item.setUnitPrice(product.getPrice());
        item.setQuantity(1);
        item.setLineTotal(product.getPrice());

        Order order = orderService.placeOrder(demo, List.of(item));
        orderService.advance(order.getId()); // PACKED
        orderService.advance(order.getId()); // SHIPPED
        orderService.advance(order.getId()); // DELIVERED

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(50));

        // --- log in, using raw locators ---
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys("demo@shopdrop.com");
        driver.findElement(By.id("password")).sendKeys("demo1234");
        driver.findElement(By.id("login-btn")).click();

        // --- go straight to the product page ---
        driver.get(baseUrl + "/products/" + product.getId());

        // --- fill in and submit the review form, using raw locators ---
        WebElement ratingSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("review-rating")));
        new Select(ratingSelect).selectByValue("4");
        driver.findElement(By.id("review-comment")).sendKeys("Sturdy and roomy, would buy again.");
        driver.findElement(By.id("submit-review-btn")).click();

        // --- verify the review now appears, via an XPath text match ---
        WebElement reviewText = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//li[@class='review-item']//p[contains(text(), 'Sturdy and roomy')]")));
        assertTrue(reviewText.isDisplayed());

        // --- and the rating summary line updated too, via a CSS selector ---
        WebElement ratingSummary = driver.findElement(By.cssSelector("#rating-summary"));
        assertTrue(ratingSummary.getText().contains("4.0"),
                "Expected the rating summary to reflect the new 4-star review, was: " + ratingSummary.getText());
    }
}
