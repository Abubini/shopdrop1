package com.shopdrop.system.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    private static final By EMAIL = By.id("email");
    private static final By PASSWORD = By.id("password");
    private static final By LOGIN_BTN = By.id("login-btn");
    public static final By ERROR = By.id("login-error");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open(String baseUrl) {
        driver.get(baseUrl + "/login");
        return this;
    }

    public void loginAs(String email, String password) {
        type(EMAIL, email);
        type(PASSWORD, password);
        click(LOGIN_BTN);
    }
}
