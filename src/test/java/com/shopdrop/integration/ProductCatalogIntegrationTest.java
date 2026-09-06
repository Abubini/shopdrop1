package com.shopdrop.integration;

import com.shopdrop.model.Category;
import com.shopdrop.repository.CategoryRepository;
import com.shopdrop.service.ProductService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Integration between ProductController, ProductService and the JPA repositories. */
@SpringBootTest
@AutoConfigureMockMvc
class ProductCatalogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void publicCanBrowseCatalogWithoutLoggingIn() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Wireless Earbuds")));
    }

    @Test
    void creatingAProductThroughTheService_makesItAppearInTheRenderedCatalog() throws Exception {
        Category category = categoryRepository.findAll().stream()
                .filter(c -> c.getName().equals("Electronics"))
                .findFirst()
                .orElseThrow();

        productService.create("Integration Test Gadget", "Created in an integration test",
                category.getId(), new BigDecimal("33.00"), 7, null);

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Integration Test Gadget")));
    }

    @Test
    void filteringByCategory_onlyShowsThatCategorysProducts() throws Exception {
        Category furniture = categoryRepository.findAll().stream()
                .filter(c -> c.getName().equals("Furniture"))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(get("/products").param("categoryId", String.valueOf(furniture.getId())))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Office Chair")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Wireless Earbuds"))));
    }
}
