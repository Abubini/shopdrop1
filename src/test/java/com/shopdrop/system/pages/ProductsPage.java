package com.shopdrop.system.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class ProductsPage extends BasePage {

    private static final By PRODUCT_GRID = By.id("product-grid");

    public ProductsPage(WebDriver driver) {
        super(driver);
    }

    public ProductsPage open(String baseUrl) {
        driver.get(baseUrl + "/products");
        return this;
    }

    public void openProductByName(String productName) {
        waitFor(PRODUCT_GRID);
        List<WebElement> cards = driver.findElements(By.cssSelector("#product-grid .product-card"));
        for (WebElement card : cards) {
            if (card.getText().contains(productName)) {
                card.click();
                return;
            }
        }
        throw new IllegalStateException("Product not found on page: " + productName);
    }
}
