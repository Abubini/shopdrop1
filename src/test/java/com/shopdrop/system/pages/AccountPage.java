package com.shopdrop.system.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class AccountPage extends BasePage {

    private static final By TOGGLE_MEMBERSHIP_BTN = By.id("toggle-membership-btn");
    private static final By MEMBERSHIP_STATUS = By.id("membership-status");

    public AccountPage(WebDriver driver) {
        super(driver);
    }

    public AccountPage open(String baseUrl) {
        driver.get(baseUrl + "/account");
        return this;
    }

    public String getMembershipStatus() {
        return waitFor(MEMBERSHIP_STATUS).getText();
    }

    public void toggleMembership() {
        click(TOGGLE_MEMBERSHIP_BTN);
    }
}
