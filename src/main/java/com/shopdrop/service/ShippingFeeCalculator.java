package com.shopdrop.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Computes the shipping fee for a cart based on its subtotal.
 *
 * Equivalence classes (by cart subtotal):
 *   LOW    [0, 20)     -> $10.00 fee
 *   MEDIUM [20, 100)   -> $5.00 fee
 *   HIGH   [100, ...)  -> free shipping
 *
 * The boundaries at 20.00 and 100.00 are deliberate targets for boundary value analysis.
 */
@Component
public class ShippingFeeCalculator {

    public static final BigDecimal LOW_BAND_LIMIT = new BigDecimal("20.00");
    public static final BigDecimal MEDIUM_BAND_LIMIT = new BigDecimal("100.00");

    public static final BigDecimal LOW_BAND_FEE = new BigDecimal("10.00");
    public static final BigDecimal MEDIUM_BAND_FEE = new BigDecimal("5.00");
    public static final BigDecimal HIGH_BAND_FEE = new BigDecimal("0.00");

    public BigDecimal calculate(BigDecimal subtotal) {
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Subtotal cannot be negative");
        }

        if (subtotal.compareTo(LOW_BAND_LIMIT) < 0) {
            return LOW_BAND_FEE;
        } else if (subtotal.compareTo(MEDIUM_BAND_LIMIT) < 0) {
            return MEDIUM_BAND_FEE;
        } else {
            return HIGH_BAND_FEE;
        }
    }
}
