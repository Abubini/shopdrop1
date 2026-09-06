package com.shopdrop.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Decision table under test:
 *
 * | # | Spend band            | Member | Discount |
 * |---|------------------------|--------|----------|
 * | 1 | LOW    (< 50)          | No     | 0%       |
 * | 2 | LOW    (< 50)          | Yes    | 5%       |
 * | 3 | MEDIUM (50 - 199.99)   | No     | 10%      |
 * | 4 | MEDIUM (50 - 199.99)   | Yes    | 15%      |
 * | 5 | HIGH   (>= 200)        | No     | 15%      |
 * | 6 | HIGH   (>= 200)        | Yes    | 20%      |
 */
class DiscountServiceTest {

    private final DiscountService discountService = new DiscountService();

    @ParameterizedTest(name = "rule: subtotal={0}, member={1} -> rate={2}")
    @CsvSource({
            "10.00,  false, 0.00",
            "10.00,  true,  0.05",
            "75.00,  false, 0.10",
            "75.00,  true,  0.15",
            "300.00, false, 0.15",
            "300.00, true,  0.20"
    })
    void decisionTableRules(String subtotal, boolean isMember, String expectedRate) {
        assertEquals(new BigDecimal(expectedRate), discountService.discountRate(new BigDecimal(subtotal), isMember));
    }

    @Test
    void calculateDiscountAmount_appliesRateToSubtotal() {
        BigDecimal amount = discountService.calculateDiscountAmount(new BigDecimal("100.00"), true);
        assertEquals(new BigDecimal("15.00"), amount);
    }

    @Test
    void negativeSubtotal_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> discountService.discountRate(new BigDecimal("-5.00"), false));
    }

    @Test
    void nullSubtotal_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> discountService.discountRate(null, false));
    }
}
