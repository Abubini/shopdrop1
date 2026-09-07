// src/test/java/com/shopdrop/system/pages/ProductsPage.java
package com.shopdrop.system.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class ProductsPage extends BasePage {

    private static final By PRODUCT_GRID = By.id("product-grid");
    private static final By SEARCH_INPUT = By.id("search-input");
    private static final By SEARCH_BTN = By.id("search-btn");

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

    public void search(String keyword) {
        type(SEARCH_INPUT, keyword);
        click(SEARCH_BTN);
    }

    public void filterByCategoryId(Long categoryId) {
        click(By.id("category-" + categoryId));
    }

    public void clearFilters() {
        click(By.id("category-all"));
    }

    public int productCount() {
        waitFor(PRODUCT_GRID);
        return driver.findElements(By.cssSelector("#product-grid .product-card")).size();
    }

    public boolean hasProductNamed(String name) {
        waitFor(PRODUCT_GRID);
        return driver.getPageSource().contains(name);
    }
}