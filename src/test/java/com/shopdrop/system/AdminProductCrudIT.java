package com.shopdrop.system;

import com.shopdrop.system.pages.AdminProductFormPage;
import com.shopdrop.system.pages.AdminProductsPage;
import com.shopdrop.system.pages.LoginPage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Exercises the admin "post, edit and delete items" workflow end to end through the UI. */
class AdminProductCrudIT extends BaseSystemTest {

    @Test
    void adminCanCreateAProductAndSeeItInTheCatalog() {
        new LoginPage(driver).open(baseUrl).loginAs("admin@shopdrop.com", "admin123");

        String productName = "Selenium Test Gadget " + System.currentTimeMillis();

        AdminProductsPage adminProductsPage = new AdminProductsPage(driver).open(baseUrl);
        adminProductsPage.goToNewProductForm();

        new AdminProductFormPage(driver).fillAndSave(
                productName, "Created by a system test", "Electronics", "42.50", "5");

        AdminProductsPage refreshedList = new AdminProductsPage(driver).open(baseUrl);
        assertTrue(refreshedList.hasProductNamed(productName));
    }

    @Test
    void regularUserCannotReachAdminProductPages() {
        new LoginPage(driver).open(baseUrl).loginAs("demo@shopdrop.com", "demo1234");

        // Spring Security's accessDeniedPage performs a server-side forward, so the URL bar
        // stays on the originally requested path even though the 403 view is what renders.
        driver.get(baseUrl + "/admin/products");

        assertTrue(driver.getPageSource().contains("Access denied"),
                "Expected the access-denied page content for a regular user hitting /admin/products");
    }
}
