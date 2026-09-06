package com.shopdrop.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Equivalence partitioning: one representative value per band.
 * Boundary value analysis: values just below, on, and just above each boundary (20.00 and 100.00).
 */
class ShippingFeeCalculatorTest {

    private final ShippingFeeCalculator calculator = new ShippingFeeCalculator();

    @Test
    void lowBand_representativeValue_returnsTenDollarFee() {
        assertEquals(new BigDecimal("10.00"), calculator.calculate(new BigDecimal("5.00")));
    }

    @Test
    void mediumBand_representativeValue_returnsFiveDollarFee() {
        assertEquals(new BigDecimal("5.00"), calculator.calculate(new BigDecimal("50.00")));
    }

    @Test
    void highBand_representativeValue_returnsFreeShipping() {
        assertEquals(new BigDecimal("0.00"), calculator.calculate(new BigDecimal("500.00")));
    }

    @ParameterizedTest(name = "subtotal={0} -> fee={1}")
    @CsvSource({
            "19.99, 10.00",
            "20.00, 5.00",
            "20.01, 5.00"
    })
    void lowToMediumBoundary(String subtotal, String expectedFee) {
        assertEquals(new BigDecimal(expectedFee), calculator.calculate(new BigDecimal(subtotal)));
    }

    @ParameterizedTest(name = "subtotal={0} -> fee={1}")
    @CsvSource({
            "99.99,  5.00",
            "100.00, 0.00",
            "100.01, 0.00"
    })
    void mediumToHighBoundary(String subtotal, String expectedFee) {
        assertEquals(new BigDecimal(expectedFee), calculator.calculate(new BigDecimal(subtotal)));
    }

    @Test
    void zeroSubtotal_isValidLowBandInput() {
        assertEquals(new BigDecimal("10.00"), calculator.calculate(BigDecimal.ZERO));
    }

    @Test
    void negativeSubtotal_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(new BigDecimal("-1.00")));
    }

    @Test
    void nullSubtotal_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(null));
    }
}
