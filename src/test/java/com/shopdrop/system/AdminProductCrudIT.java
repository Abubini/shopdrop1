// src/test/java/com/shopdrop/system/AdminProductCrudIT.java
package com.shopdrop.system;

import com.shopdrop.model.Product;
import com.shopdrop.repository.ProductRepository;
import com.shopdrop.system.pages.AdminProductFormPage;
import com.shopdrop.system.pages.AdminProductsPage;
import com.shopdrop.system.pages.LoginPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Exercises the admin "post, edit and delete items" workflow end to end through the UI. */
class AdminProductCrudIT extends BaseSystemTest {

    @Autowired
    private ProductRepository productRepository;

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
    void adminCanDeleteAProduct() {
        new LoginPage(driver).open(baseUrl).loginAs("admin@shopdrop.com", "admin123");

        String productName = "Deletable Gadget " + System.currentTimeMillis();

        AdminProductsPage adminProductsPage = new AdminProductsPage(driver).open(baseUrl);
        adminProductsPage.goToNewProductForm();
        new AdminProductFormPage(driver).fillAndSave(
                productName, "Will be deleted", "Electronics", "9.99", "3");

        Product created = productRepository.findAll().stream()
                .filter(p -> p.getName().equals(productName))
                .findFirst()
                .orElseThrow();

        adminProductsPage = new AdminProductsPage(driver).open(baseUrl);
        assertTrue(adminProductsPage.hasProductNamed(productName));

        adminProductsPage.deleteProduct(created.getId());

        adminProductsPage = new AdminProductsPage(driver).open(baseUrl);
        assertFalse(adminProductsPage.hasProductNamed(productName),
                "Expected the product to be gone from the catalog after deleting it");
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