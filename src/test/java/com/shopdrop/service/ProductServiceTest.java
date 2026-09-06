package com.shopdrop.service;

import com.shopdrop.model.Category;
import com.shopdrop.model.Product;
import com.shopdrop.repository.CategoryRepository;
import com.shopdrop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Tests ProductService's CRUD validation in isolation with mocked repositories. */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private ProductService productService;
    private Category category;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, categoryRepository);
        category = new Category();
        category.setId(1L);
        category.setName("Electronics");
        lenient().when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void create_withValidData_savesProduct() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Product product = productService.create("Gadget", "A gadget", 1L, new BigDecimal("19.99"), 10, "\uD83D\uDCE6");

        assertEquals("Gadget", product.getName());
        assertEquals(category, product.getCategory());
    }

    @Test
    void create_withMissingCategoryId_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> productService.create("Gadget", "A gadget", null, new BigDecimal("19.99"), 10, null));
    }

    @Test
    void create_withUnknownCategory_throwsException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> productService.create("Gadget", "A gadget", 99L, new BigDecimal("19.99"), 10, null));
    }

    @Test
    void create_withBlankName_throwsException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        assertThrows(IllegalArgumentException.class,
                () -> productService.create("   ", "A gadget", 1L, new BigDecimal("19.99"), 10, null));
    }

    @Test
    void create_withZeroPrice_throwsException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        assertThrows(IllegalArgumentException.class,
                () -> productService.create("Gadget", "A gadget", 1L, BigDecimal.ZERO, 10, null));
    }

    @Test
    void create_withNegativePrice_throwsException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        assertThrows(IllegalArgumentException.class,
                () -> productService.create("Gadget", "A gadget", 1L, new BigDecimal("-5.00"), 10, null));
    }

    @Test
    void create_withNegativeStock_throwsException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        assertThrows(IllegalArgumentException.class,
                () -> productService.create("Gadget", "A gadget", 1L, new BigDecimal("19.99"), -1, null));
    }

    @Test
    void create_withBlankIcon_fallsBackToDefaultIcon() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        Product product = productService.create("Gadget", "A gadget", 1L, new BigDecimal("19.99"), 10, "   ");
        assertEquals("\uD83D\uDCE6", product.getIcon());
    }

    @Test
    void update_changesFieldsOnExistingProduct() {
        Product existing = new Product("Old Name", "Old desc", category, new BigDecimal("5.00"), 3, "\uD83D\uDCE6");
        existing.setId(5L);
        when(productRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Product updated = productService.update(5L, "New Name", "New desc", 1L, new BigDecimal("25.00"), 8, null);

        assertEquals("New Name", updated.getName());
        assertEquals(new BigDecimal("25.00"), updated.getPrice());
        assertEquals(8, updated.getStockQuantity());
        assertEquals("\uD83D\uDCE6", updated.getIcon());
    }

    @Test
    void update_onUnknownProduct_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> productService.update(99L, "Name", "Desc", 1L, new BigDecimal("10.00"), 1, null));
    }

    @Test
    void delete_onExistingProduct_removesIt() {
        when(productRepository.existsById(5L)).thenReturn(true);
        productService.delete(5L);
        verify(productRepository).deleteById(5L);
    }

    @Test
    void delete_onUnknownProduct_throwsException() {
        when(productRepository.existsById(99L)).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> productService.delete(99L));
    }

    @Test
    void findById_onUnknownProduct_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> productService.findById(99L));
    }

    @Test
    void search_withNoFilters_returnsAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(
                new Product("Mug", "d", category, new BigDecimal("5.00"), 1, "x"),
                new Product("Lamp", "d", category, new BigDecimal("5.00"), 1, "x")));

        List<Product> results = productService.search(null, null);

        assertEquals(2, results.size());
    }

    @Test
    void search_withQuery_filtersByNameCaseInsensitively() {
        when(productRepository.findAll()).thenReturn(List.of(
                new Product("Wireless Earbuds", "d", category, new BigDecimal("5.00"), 1, "x"),
                new Product("Ceramic Mug", "d", category, new BigDecimal("5.00"), 1, "x")));

        List<Product> results = productService.search(null, "EARBUDS");

        assertEquals(1, results.size());
        assertEquals("Wireless Earbuds", results.get(0).getName());
    }

    @Test
    void search_withCategoryId_delegatesToCategoryLookup() {
        when(productRepository.findByCategoryId(1L)).thenReturn(List.of(
                new Product("Mug", "d", category, new BigDecimal("5.00"), 1, "x")));

        List<Product> results = productService.search(1L, null);

        assertEquals(1, results.size());
        verify(productRepository).findByCategoryId(1L);
    }

    @Test
    void search_withBlankQuery_returnsUnfilteredResults() {
        when(productRepository.findAll()).thenReturn(List.of(
                new Product("Mug", "d", category, new BigDecimal("5.00"), 1, "x")));

        List<Product> results = productService.search(null, "   ");

        assertEquals(1, results.size());
    }

    @Test
    void decreaseStock_reducesQuantityAndSaves() {
        Product product = new Product("Mug", "d", category, new BigDecimal("5.00"), 10, "x");

        productService.decreaseStock(product, 3);

        assertEquals(7, product.getStockQuantity());
        verify(productRepository).save(product);
    }

    @Test
    void decreaseStock_toExactlyZero_succeeds() {
        Product product = new Product("Mug", "d", category, new BigDecimal("5.00"), 5, "x");
        productService.decreaseStock(product, 5);
        assertEquals(0, product.getStockQuantity());
    }

    @Test
    void decreaseStock_belowZero_throwsExceptionAndLeavesStockUnchanged() {
        Product product = new Product("Mug", "d", category, new BigDecimal("5.00"), 2, "x");

        assertThrows(IllegalArgumentException.class, () -> productService.decreaseStock(product, 3));
        assertEquals(2, product.getStockQuantity());
    }

    @Test
    void increaseStock_addsQuantityAndSaves() {
        Product product = new Product("Mug", "d", category, new BigDecimal("5.00"), 4, "x");

        productService.increaseStock(product, 6);

        assertEquals(10, product.getStockQuantity());
        verify(productRepository).save(product);
    }
}
