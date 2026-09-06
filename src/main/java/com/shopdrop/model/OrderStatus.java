package com.shopdrop.model;

/**
 * The lifecycle states of an order.
 *
 * Allowed transitions (enforced in OrderService):
 *   PLACED    -> PACKED, CANCELLED
 *   PACKED    -> SHIPPED, CANCELLED
 *   SHIPPED   -> DELIVERED
 *   DELIVERED -> (terminal)
 *   CANCELLED -> (terminal)
 */
public enum OrderStatus {
    PLACED,
    PACKED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
