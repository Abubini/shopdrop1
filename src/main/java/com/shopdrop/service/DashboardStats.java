package com.shopdrop.service;

import com.shopdrop.model.OrderStatus;

import java.math.BigDecimal;
import java.util.Map;

/** A simple read-only snapshot of store-wide numbers for the admin dashboard. */
public class DashboardStats {

    private final long totalProducts;
    private final long totalUsers;
    private final long totalOrders;
    private final Map<OrderStatus, Long> ordersByStatus;
    private final BigDecimal totalRevenue;

    public DashboardStats(long totalProducts, long totalUsers, long totalOrders,
                           Map<OrderStatus, Long> ordersByStatus, BigDecimal totalRevenue) {
        this.totalProducts = totalProducts;
        this.totalUsers = totalUsers;
        this.totalOrders = totalOrders;
        this.ordersByStatus = ordersByStatus;
        this.totalRevenue = totalRevenue;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public Map<OrderStatus, Long> getOrdersByStatus() {
        return ordersByStatus;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
}
