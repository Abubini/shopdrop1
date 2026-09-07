// src/test/java/com/shopdrop/system/SearchAndCategoryFilterIT.java
package com.shopdrop.system;

import com.shopdrop.repository.CategoryRepository;
import com.shopdrop.system.pages.ProductsPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Covers the search box and category filter pills that ShoppingJourneyIT doesn't exercise. */
class SearchAndCategoryFilterIT extends BaseSystemTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void searchingByNameNarrowsTheCatalog() {
        ProductsPage productsPage = new ProductsPage(driver).open(baseUrl);
        int totalCount = productsPage.productCount();

        productsPage.search("Earbuds");

        assertTrue(productsPage.hasProductNamed("Wireless Earbuds"));
        assertTrue(productsPage.productCount() < totalCount,
                "Expected the search to narrow the catalog down from " + totalCount + " products");
    }

    @Test
    void searchingForSomethingThatDoesNotExist_showsNoResults() {
        ProductsPage productsPage = new ProductsPage(driver).open(baseUrl);
        productsPage.search("this-product-does-not-exist-xyz");

        assertTrue(driver.getPageSource().contains("No products match your search"));
    }

    @Test
    void filteringByCategory_onlyShowsThatCategorysProducts() {
        Long furnitureId = categoryRepository.findAll().stream()
                .filter(c -> c.getName().equals("Furniture"))
                .findFirst()
                .orElseThrow()
                .getId();

        ProductsPage productsPage = new ProductsPage(driver).open(baseUrl);
        productsPage.filterByCategoryId(furnitureId);

        assertTrue(productsPage.hasProductNamed("Office Chair"));
        assertTrue(productsPage.hasProductNamed("Standing Desk"));
    }
}