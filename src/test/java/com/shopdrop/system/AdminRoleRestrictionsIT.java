package com.shopdrop.system;

import com.shopdrop.system.pages.LoginPage;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Confirms admins are shown as staff, not shoppers: no Cart nav link, and the route is blocked. */
class AdminRoleRestrictionsIT extends BaseSystemTest {

    @Test
    void adminLoginLandsOnTheDashboardAndHidesTheCartLink() {
        new LoginPage(driver).open(baseUrl).loginAs("admin@shopdrop.com", "admin123");

        assertTrue(driver.getCurrentUrl().endsWith("/admin"),
                "Expected admin login to land on /admin, was: " + driver.getCurrentUrl());
        assertTrue(driver.findElements(By.id("nav-cart")).isEmpty(),
                "Expected no Cart nav link to be rendered for an admin account");
    }

    @Test
    void adminNavigatingDirectlyToCart_seesAccessDenied() {
        new LoginPage(driver).open(baseUrl).loginAs("admin@shopdrop.com", "admin123");

        driver.get(baseUrl + "/cart");

        assertTrue(driver.getPageSource().contains("Access denied"));
    }

    @Test
    void regularUserSeesTheCartLinkAndNoAdminLinks() {
        new LoginPage(driver).open(baseUrl).loginAs("demo@shopdrop.com", "demo1234");

        assertFalse(driver.findElements(By.id("nav-cart")).isEmpty(),
                "Expected the Cart nav link to be visible for a regular user");
        assertTrue(driver.findElements(By.id("nav-admin-dashboard")).isEmpty(),
                "Expected no admin dashboard link to be rendered for a regular user");
    }
}
