package com.shopdrop.system.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class RegisterPage extends BasePage {

    private static final By NAME = By.id("name");
    private static final By EMAIL = By.id("email");
    private static final By PASSWORD = By.id("password");
    private static final By CONFIRM_PASSWORD = By.id("confirmPassword");
    private static final By REGISTER_BTN = By.id("register-btn");
    public static final By ERROR = By.id("register-error");

    public RegisterPage(WebDriver driver) {
        super(driver);
    }

    public RegisterPage open(String baseUrl) {
        driver.get(baseUrl + "/register");
        return this;
    }

    public void register(String name, String email, String password, String confirmPassword) {
        type(NAME, name);
        type(EMAIL, email);
        type(PASSWORD, password);
        type(CONFIRM_PASSWORD, confirmPassword);
        click(REGISTER_BTN);
    }
}
