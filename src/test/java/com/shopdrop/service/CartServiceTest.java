package com.shopdrop.service;

import com.shopdrop.model.Category;
import com.shopdrop.model.Product;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Tests CartService in isolation using a Mockito mock (test double) for ProductRepository. */
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ProductRepository productRepository;

    private CartService cartService;
    private Product product;
    private Category homeCategory;

    @BeforeEach
    void setUp() {
        cartService = new CartService(productRepository);
        homeCategory = new Category();
        homeCategory.setId(1L);
        homeCategory.setName("Home");
        product = new Product("Mug", "A mug", homeCategory, new BigDecimal("12.00"), 10, "\u2615");
        product.setId(1L);
    }

    @Test
    void addItem_newProduct_addsSingleLine() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        cartService.addItem(1L, 2);

        List<CartService.CartLine> lines = cartService.getLines();
        assertEquals(1, lines.size());
        assertEquals(2, lines.get(0).getQuantity());
        assertEquals(new BigDecimal("24.00"), lines.get(0).getLineTotal());
    }

    @Test
    void addItem_sameProductTwice_mergesQuantity() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        cartService.addItem(1L, 1);
        cartService.addItem(1L, 3);

        assertEquals(4, cartService.getLines().get(0).getQuantity());
    }

    @Test
    void addItem_zeroQuantity_throwsExceptionAndDoesNotTouchRepository() {
        assertThrows(IllegalArgumentException.class, () -> cartService.addItem(1L, 0));
        verifyNoInteractions(productRepository);
    }

    @Test
    void addItem_negativeQuantity_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> cartService.addItem(1L, -1));
    }

    @Test
    void addItem_unknownProduct_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> cartService.addItem(99L, 1));
    }

    @Test
    void removeItem_removesLine() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        cartService.addItem(1L, 1);
        cartService.removeItem(1L);
        assertTrue(cartService.isEmpty());
    }

    @Test
    void updateQuantity_toZero_removesLine() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        cartService.addItem(1L, 1);
        cartService.updateQuantity(1L, 0);
        assertTrue(cartService.isEmpty());
    }

    @Test
    void updateQuantity_toPositive_updatesLine() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        cartService.addItem(1L, 1);
        cartService.updateQuantity(1L, 5);
        assertEquals(5, cartService.getLines().get(0).getQuantity());
    }

    @Test
    void getSubtotal_sumsAllLines() {
        Product second = new Product("Bottle", "A bottle", homeCategory, new BigDecimal("18.75"), 5, "\uD83D\uDEB0");
        second.setId(2L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findById(2L)).thenReturn(Optional.of(second));

        cartService.addItem(1L, 2);
        cartService.addItem(2L, 1);

        assertEquals(new BigDecimal("42.75"), cartService.getSubtotal());
    }

    @Test
    void clear_emptiesCart() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        cartService.addItem(1L, 1);
        cartService.clear();
        assertTrue(cartService.isEmpty());
    }

    @Test
    void newCart_isEmpty() {
        assertTrue(cartService.isEmpty());
        assertEquals(BigDecimal.ZERO, cartService.getSubtotal());
    }

    @Test
    void addItem_exactlyAtStockLimit_succeeds() {
        product.setStockQuantity(5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        cartService.addItem(1L, 5);

        assertEquals(5, cartService.getLines().get(0).getQuantity());
    }

    @Test
    void addItem_exceedingAvailableStock_throwsException() {
        product.setStockQuantity(5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(IllegalArgumentException.class, () -> cartService.addItem(1L, 6));
    }

    @Test
    void addItem_cumulativeQuantityExceedingStock_throwsException() {
        product.setStockQuantity(5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        cartService.addItem(1L, 3);
        assertThrows(IllegalArgumentException.class, () -> cartService.addItem(1L, 3));
    }

    @Test
    void updateQuantity_exceedingStock_throwsException() {
        product.setStockQuantity(5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        cartService.addItem(1L, 2);
        assertThrows(IllegalArgumentException.class, () -> cartService.updateQuantity(1L, 6));
    }

    @Test
    void updateQuantity_atExactStockLimit_succeeds() {
        product.setStockQuantity(5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        cartService.addItem(1L, 1);
        cartService.updateQuantity(1L, 5);

        assertEquals(5, cartService.getLines().get(0).getQuantity());
    }
}
