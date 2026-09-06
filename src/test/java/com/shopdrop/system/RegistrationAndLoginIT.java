package com.shopdrop.system;

import com.shopdrop.system.pages.AccountPage;
import com.shopdrop.system.pages.LoginPage;
import com.shopdrop.system.pages.RegisterPage;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistrationAndLoginIT extends BaseSystemTest {

    @Test
    void userCanRegisterThenLogIn() {
        String uniqueEmail = "selenium.user." + System.currentTimeMillis() + "@shopdrop.com";

        new RegisterPage(driver).open(baseUrl)
                .register("Selenium Tester", uniqueEmail, "password123", "password123");

        new LoginPage(driver).loginAs(uniqueEmail, "password123");

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/account"));

        new AccountPage(driver);
        assertTrue(driver.getCurrentUrl().endsWith("/account"),
                "Expected to land on the account page after login, was: " + driver.getCurrentUrl());
        assertTrue(driver.getPageSource().contains(uniqueEmail));
    }

    @Test
    void registeringWithAnAlreadyUsedEmail_showsAnError() {
        new RegisterPage(driver).open(baseUrl)
                .register("Demo Shopper", "demo@shopdrop.com", "password123", "password123");

        assertTrue(driver.findElement(RegisterPage.ERROR).isDisplayed());
    }

    @Test
    void loggingInWithWrongPassword_showsAnError() {
        new LoginPage(driver).open(baseUrl).loginAs("demo@shopdrop.com", "wrong-password");

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(LoginPage.ERROR));

        assertTrue(driver.findElement(LoginPage.ERROR).isDisplayed());
    }
}