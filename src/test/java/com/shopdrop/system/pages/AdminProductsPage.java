// src/test/java/com/shopdrop/system/pages/AdminProductsPage.java
package com.shopdrop.system.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class AdminProductsPage extends BasePage {

    private static final By NEW_PRODUCT_BTN = By.id("new-product-btn");

    public AdminProductsPage(WebDriver driver) {
        super(driver);
    }

    public AdminProductsPage open(String baseUrl) {
        driver.get(baseUrl + "/admin/products");
        return this;
    }

    public void goToNewProductForm() {
        click(NEW_PRODUCT_BTN);
    }

    public boolean hasProductNamed(String name) {
        waitFor(By.id("admin-products-table"));
        return driver.getPageSource().contains(name);
    }

    public void editProduct(Long productId) {
        click(By.id("edit-btn-" + productId));
    }

    public void deleteProduct(Long productId) {
        click(By.id("delete-btn-" + productId));
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
    }
}