package com.shopdrop.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Computes the order discount from a small decision table combining the cart's
 * spend band with the customer's membership status.
 *
 * | # | Spend band                 | Member | Discount |
 * |---|-----------------------------|--------|----------|
 * | 1 | LOW    (subtotal < 50)      | No     | 0%       |
 * | 2 | LOW    (subtotal < 50)      | Yes    | 5%       |
 * | 3 | MEDIUM (50 <= subtotal<200) | No     | 10%      |
 * | 4 | MEDIUM (50 <= subtotal<200) | Yes    | 15%      |
 * | 5 | HIGH   (subtotal >= 200)    | No     | 15%      |
 * | 6 | HIGH   (subtotal >= 200)    | Yes    | 20%      |
 */
@Component
public class DiscountService {

    public static final BigDecimal MEDIUM_THRESHOLD = new BigDecimal("50.00");
    public static final BigDecimal HIGH_THRESHOLD = new BigDecimal("200.00");

    public BigDecimal discountRate(BigDecimal subtotal, boolean isMember) {
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Subtotal cannot be negative");
        }

        SpendBand band = bandFor(subtotal);

        switch (band) {
            case LOW:
                return isMember ? new BigDecimal("0.05") : new BigDecimal("0.00");
            case MEDIUM:
                return isMember ? new BigDecimal("0.15") : new BigDecimal("0.10");
            case HIGH:
                return isMember ? new BigDecimal("0.20") : new BigDecimal("0.15");
            default:
                throw new IllegalStateException("Unknown spend band: " + band);
        }
    }

    public BigDecimal calculateDiscountAmount(BigDecimal subtotal, boolean isMember) {
        BigDecimal rate = discountRate(subtotal, isMember);
        return subtotal.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private SpendBand bandFor(BigDecimal subtotal) {
        if (subtotal.compareTo(MEDIUM_THRESHOLD) < 0) {
            return SpendBand.LOW;
        } else if (subtotal.compareTo(HIGH_THRESHOLD) < 0) {
            return SpendBand.MEDIUM;
        } else {
            return SpendBand.HIGH;
        }
    }

    private enum SpendBand {
        LOW, MEDIUM, HIGH
    }
}