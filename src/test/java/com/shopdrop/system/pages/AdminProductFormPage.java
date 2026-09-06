package com.shopdrop.system.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

public class AdminProductFormPage extends BasePage {

    private static final By NAME = By.id("name");
    private static final By DESCRIPTION = By.id("description");
    private static final By CATEGORY = By.id("categoryId");
    private static final By PRICE = By.id("price");
    private static final By STOCK = By.id("stockQuantity");
    private static final By SAVE_BTN = By.id("save-product-btn");

    public AdminProductFormPage(WebDriver driver) {
        super(driver);
    }

    public void fillAndSave(String name, String description, String categoryName, String price, String stock) {
        type(NAME, name);
        type(DESCRIPTION, description);
        new Select(waitFor(CATEGORY)).selectByVisibleText(categoryName);
        type(PRICE, price);
        type(STOCK, stock);
        click(SAVE_BTN);
    }
}
